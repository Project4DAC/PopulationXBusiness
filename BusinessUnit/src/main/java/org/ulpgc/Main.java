package org.ulpgc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ulpgc.business.interfaces.*;
import org.ulpgc.business.operations.DAO.*;
import org.ulpgc.business.operations.POJO.Dato;
import org.ulpgc.business.operations.POJO.Indicador;
import org.ulpgc.business.operations.POJO.Operacion;
import org.ulpgc.business.operations.POJO.TablasOperacion;
import org.ulpgc.business.query.ActiveMQConnector;
import org.ulpgc.business.query.BormeTableCommandFactory;
import org.ulpgc.business.query.Config;
import org.ulpgc.business.query.INETableCommandFactory;
import org.ulpgc.business.repository.*;
import org.ulpgc.business.service.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    private static HikariDataSource ineDataSource;
    private static HikariDataSource bormeDataSource;
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static MessageProcessor messageProcessor;
    private static DataProcessor dataProcessor;
    private static ExecutorService executorService;
    private static Properties config;

    public static class DatabaseUtil {
        public static HikariDataSource createDataSource(String dbPath, String poolName) {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:sqlite:" + dbPath);
                config.setDriverClassName("org.sqlite.JDBC");
                config.setMaximumPoolSize(10);
                config.setPoolName(poolName);

                HikariDataSource dataSource = new HikariDataSource(config);

                // Initialize database schema
                try (Connection connection = dataSource.getConnection()) {
                    String createTableSQL = "CREATE TABLE IF NOT EXISTS json_data (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "function_name TEXT, " +
                            "message TEXT)";
                    try (PreparedStatement statement = connection.prepareStatement(createTableSQL)) {
                        statement.executeUpdate();
                    }
                } catch (SQLException e) {
                    LOGGER.severe("Error creating table: " + e.getMessage());
                    throw new RuntimeException("Failed to initialize database schema", e);
                }

                return dataSource;
            } catch (Exception e) {
                LOGGER.severe("Failed to create data source: " + e.getMessage());
                throw new RuntimeException("Cannot create data source", e);
            }
        }
    }

    public static void main(String[] args) {
        configureLogging();
        loadConfiguration();

        try {
            // Initialize resources
            initializeResources();

            // Start services based on mode
            if (isBackgroundMode(args)) {
                LOGGER.info("Starting in background mode");
                runBackgroundService();
            } else {
                LOGGER.info("Starting in interactive mode");
                runInteractiveMode();
            }
        } catch (Exception e) {
            LOGGER.severe("Fatal error in main: " + e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }

    private static void loadConfiguration() {
        config = new Properties();

        // Default configurations
        config.setProperty("ACTIVEMQ_USERNAME", "admin");
        config.setProperty("ACTIVEMQ_PASSWORD", "admin");
        config.setProperty("ACTIVEMQ_URL", "tcp://localhost:61616");
        config.setProperty("INE_DB_PATH", "./INE.db");
        config.setProperty("BORME_DB_PATH", "./Borme.db");
        config.setProperty("DATA_MART_ROOT", System.getProperty("user.home") + File.separator + "ine-datamart");
        config.setProperty("PROCESSING_INTERVAL_HOURS", "6");

        // Try to load from properties file
        File configFile = new File("application.properties");
        if (configFile.exists()) {
            try (InputStream input = new FileInputStream(configFile)) {
                config.load(input);
                LOGGER.info("Loaded configuration from application.properties");
            } catch (IOException e) {
                LOGGER.warning("Failed to load configuration file: " + e.getMessage());
                LOGGER.warning("Using default configuration");
            }
        } else {
            LOGGER.info("No configuration file found, using default configuration");
        }

        // Override with environment variables if available
        overrideConfigWithEnvVars();
    }

    private static void overrideConfigWithEnvVars() {
        Map<String, String> envVars = new HashMap<>(System.getenv());
        for (String key : envVars.keySet()) {
            if (config.containsKey(key)) {
                config.setProperty(key, envVars.get(key));
            }
        }
    }

    private static String getConfigProperty(String key) {
        return config.getProperty(key);
    }

    private static void initializeResources() {
        // Initialize executor service
        executorService = Executors.newCachedThreadPool();

        // Initialize data sources
        String ineDbPath = getConfigProperty("INE_DB_PATH");
        String bormeDbPath = getConfigProperty("BORME_DB_PATH");

        try {
            ineDataSource = DatabaseUtil.createDataSource(ineDbPath, "IneDbPool");
            INETableCommandFactory.createInitializeDatabaseCommand(ineDataSource.getConnection()).execute();
            LOGGER.info("INE Database initialized successfully");

            bormeDataSource = DatabaseUtil.createDataSource(bormeDbPath, "BormeDbPool");
            BormeTableCommandFactory.createInitializeDatabaseCommand(bormeDataSource.getConnection()).execute();
            LOGGER.info("BORME Database initialized successfully");
        } catch (Exception e) {
            LOGGER.severe("Failed to initialize databases: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }

        // Initialize message processor
        initializeMessageProcessor();

        // Initialize data processor
        String dataMartRoot = getConfigProperty("DATA_MART_ROOT");
        dataProcessor = new INEDataMartProcessor(dataMartRoot, ineDataSource);

        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
    }

    private static void initializeMessageProcessor() {
        String username = getConfigProperty("ACTIVEMQ_USERNAME");
        String password = getConfigProperty("ACTIVEMQ_PASSWORD");
        String brokerUrl = getConfigProperty("ACTIVEMQ_URL");
        List<String> topicNames = Arrays.asList(
                "INE_OPERACIONES_QUEUE",
                "INE_TABLAS_OPERACION_QUEUE"
        );

        Config mqConfig = new Config(username, password, brokerUrl, topicNames);
        MessageBrokerConnector brokerConnector = new ActiveMQConnector(mqConfig);
        MessageSaver messageSaver = new DatabaseOutput(ineDataSource);

        messageProcessor = new MessageProcessor(brokerConnector, messageSaver);
    }

    private static boolean isBackgroundMode(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--background") || arg.equalsIgnoreCase("-b")) {
                return true;
            }
        }
        return false;
    }

    private static void runBackgroundService() {
        try {
            // Start message processing
            messageProcessor.startProcessing();
            LOGGER.info("Message processor started successfully");

            // Start automatic data processing
            setupAutomaticProcessing();

            // Keep service running
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Background service interrupted: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error in background service: " + e.getMessage());
        }
    }

    private static void runInteractiveMode() {
        try {
            LOGGER.info("Initializing BusXpop database...");
            DatabaseInitializer.initializeDatabase();

            APIService apiService = new APIService();

            OperacionRepository operacionRepository = RepositoryFactory.getRepository(OperacionRepository.class);
            TablaRepository tablaRepository = RepositoryFactory.getRepository(TablaRepository.class);

            try (Scanner scanner = new Scanner(System.in)) {
                boolean running = true;

                while (running) {
                    try {
                        displayMenu();
                        int choice = readIntInput(scanner, 0, 6);

                        switch (choice) {
                            case 1 -> listIndicators(apiService);
                            case 2 -> viewIndicatorData(scanner, apiService);
                            case 3 -> filterIndicatorData(scanner, apiService);
                            case 4 -> viewIndicatorMetadata(scanner, apiService);
                            case 5 -> exportIndicatorToCSV(scanner, apiService);
                            case 0 -> running = false;
                        }
                    } catch (Exception e) {
                        LOGGER.warning("Error in menu operation: " + e.getMessage());
                        System.out.println("An error occurred: " + e.getMessage());
                    }
                }
            }

            shutdown();

        } catch (Exception e) {
            LOGGER.severe("BusXpop application error: " + e.getMessage());
            e.printStackTrace();
            shutdown();
        }
    }

    private static void listIndicators(APIService apiService) {
        List<Indicador> indicators = apiService.getAllIndicators();
        System.out.println("\nAvailable Indicators:");
        indicators.forEach(indicator -> System.out.println(indicator.getCod() + ": " + indicator.getNombre()));
    }

    private static void viewIndicatorData(Scanner scanner, APIService apiService) {
        System.out.print("Enter indicator code: ");
        String code = scanner.nextLine().trim();

        List<Dato> data = apiService.getIndicatorData(code);
        if (data.isEmpty()) {
            System.out.println("No data found for indicator " + code);
        } else {
            data.forEach(d -> System.out.println(d.getFecha() + ": " + d.getUnidad() + " " + d.getUnidad()));
        }
    }

    private static void filterIndicatorData(Scanner scanner, APIService apiService) {
        System.out.print("Enter indicator code: ");
        String code = scanner.nextLine().trim();

        System.out.print("Enter start year: ");
        int startYear = readIntInput(scanner, 1900, 2100);

        System.out.print("Enter end year: ");
        int endYear = readIntInput(scanner, startYear, 2100);

        List<Dato> data = apiService.getIndicatorDataFiltered(code, startYear, endYear);
        if (data.isEmpty()) {
            System.out.println("No data found in the given range.");
        } else {
            data.forEach(d -> System.out.println(d.getFecha() + ": " + d.getUnidad() + " " + d.getUnidad()));
        }
    }

    private static void viewIndicatorMetadata(Scanner scanner, APIService apiService) {
        System.out.print("Enter indicator code: ");
        String code = scanner.nextLine().trim();

        Optional<Indicador> indicator = apiService.getIndicatorMetadata(code);
        indicator.ifPresentOrElse(
                ind -> System.out.println("Code: " + ind.getCod() + "\nName: " + ind.getNombre() +

                         "\nUnit: " + ind.getFkUnidad()),
                () -> System.out.println("Indicator not found.")
        );
    }

    private static void exportIndicatorToCSV(Scanner scanner, APIService apiService) {
        System.out.print("Enter indicator code: ");
        String code = scanner.nextLine().trim();

        boolean success = apiService.exportIndicatorDataToCSV(code);
        if (success) {
            System.out.println("Data exported successfully to CSV.");
        } else {
            System.out.println("Failed to export data.");
        }
    }


    private static void displayMenu() {
        System.out.println("\n=== Socioeconomic Data Consultation ===");
        System.out.println("1. List available indicators");
        System.out.println("2. View indicator data by code");
        System.out.println("3. Filter indicator data by year range");
        System.out.println("4. View indicator metadata");
        System.out.println("5. Export indicator data to CSV");
        System.out.println("0. Return to main menu");
        System.out.print("Enter your choice: ");
    }

    private static int readIntInput(Scanner scanner, int min, int max) {
        int value;
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.println("Please enter a number between " + min + " and " + max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static void fetchOperationByCode(Scanner scanner, APIService apiService) {
        System.out.print("Enter operation code: ");
        String operacionCodigo = scanner.nextLine().trim();

        if (operacionCodigo.isEmpty()) {
            System.out.println("Operation code cannot be empty");
            return;
        }

        Optional<Operacion> operacion = apiService.fetchAndSaveOperacion(operacionCodigo);

        if (operacion.isPresent()) {
            Operacion op = operacion.get();
            System.out.println("Operation fetched and saved:");
            System.out.println("ID: " + op.getId());
            System.out.println("Code: " + op.getCodigo());
            System.out.println("Name: " + op.getNombre());
            System.out.println("IOE Code: " + op.getCodIoE());
            System.out.println("URL: " + op.getUrlOperacion());
        } else {
            System.out.println("Operation not found or error occurred.");
        }
    }

    private static void listAllOperations(OperacionRepository operacionRepository) {
        List<Operacion> operacions = operacionRepository.findAll();
        System.out.println("\nAll operations:");

        if (operacions.isEmpty()) {
            System.out.println("No operations found. Try fetching some first.");
        } else {
            for (Operacion op : operacions) {
                System.out.println(op.getId() + ": " + op.getNombre() + " (" + op.getCodigo() + ")");
            }
        }
    }

    private static void fetchTablesForOperation(Scanner scanner, APIService apiService,
                                                OperacionRepository operacionRepository,
                                                TablaRepository tablaRepository) {
        System.out.print("Enter operation code: ");
        String codigo = scanner.nextLine().trim();

        if (codigo.isEmpty()) {
            System.out.println("Operation code cannot be empty");
            return;
        }

        int tablesCount = apiService.fetchAndSaveTablasOperacion(codigo);
        System.out.println("Fetched and saved " + tablesCount + " tables.");

        // Get the operation ID and list the tables
        Optional<Operacion> op = operacionRepository.findByCodigo(codigo);
        if (op.isPresent()) {
            int operacionId = op.get().getId();
            List<TablasOperacion> tablas = tablaRepository.findByOperacionId(operacionId);

            System.out.println("\nTables for operation " + codigo + ":");
            for (TablasOperacion tabla : tablas) {
                System.out.println(tabla.getId() + ": " + tabla.getNombre());
            }
        } else {
            System.out.println("Operation not found for code: " + codigo);
        }
    }

    private static void fetchDataForTable(Scanner scanner, APIService apiService) {
        try {
            System.out.print("Enter table ID: ");
            int tablaId = readIntInput(scanner, 1, Integer.MAX_VALUE);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void processINEData() {
        if (dataProcessor != null) {
            System.out.println("Processing INE data now...");
            try {
                dataProcessor.process();
                System.out.println("INE data processing completed.");
            } catch (Exception e) {
                System.out.println("Error processing INE data: " + e.getMessage());
                LOGGER.warning("Error processing INE data: " + e.getMessage());
            }
        } else {
            System.out.println("INE Data Processor not initialized.");
        }
    }

    private static void configureLogging() {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);

        // Remove default handlers
        for (java.util.logging.Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        // Add console handler with simple formatter
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(consoleHandler);
    }

    private static void setupAutomaticProcessing() {
        int processingIntervalHours;
        try {
            processingIntervalHours = Integer.parseInt(getConfigProperty("PROCESSING_INTERVAL_HOURS"));
        } catch (NumberFormatException e) {
            processingIntervalHours = 6; // Default to 6 hours
            LOGGER.warning("Invalid processing interval configured, using default of 6 hours");
        }

        final int interval = processingIntervalHours;

        executorService.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // Sleep for configured interval
                        LOGGER.info("Scheduled data processing will run in " + interval + " hours");
                        Thread.sleep(interval * 60 * 60 * 1000L);

                        // Process the data
                        LOGGER.info("Starting scheduled data processing...");
                        dataProcessor.process();
                        LOGGER.info("Scheduled data processing completed");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        LOGGER.info("Automatic processing thread interrupted");
                        break;
                    } catch (Exception e) {
                        LOGGER.warning("Error in scheduled processing: " + e.getMessage());
                        // Continue with the next iteration
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Fatal error in processing thread: " + e.getMessage());
            }
        });

        LOGGER.info("Automatic data processing scheduled every " + processingIntervalHours + " hours");
    }

    public static synchronized void shutdown() {
        LOGGER.info("Service shutdown initiated...");

        // Prevent multiple shutdown calls
        if (shutdownLatch.getCount() == 0) {
            return;
        }

        try {
            // Stop message processor
            if (messageProcessor != null && messageProcessor.isRunning()) {
                try {
                    messageProcessor.stopProcessing();
                    LOGGER.info("Message processor stopped");
                } catch (Exception e) {
                    LOGGER.warning("Error stopping message processor: " + e.getMessage());
                }
            }

            // Shutdown executor service
            if (executorService != null && !executorService.isShutdown()) {
                try {
                    executorService.shutdown();
                    if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                        executorService.shutdownNow();
                    }
                    LOGGER.info("Executor service shutdown complete");
                } catch (Exception e) {
                    LOGGER.warning("Error shutting down executor service: " + e.getMessage());
                }
            }

            // Close database connections
            try {
                DatabaseConnectionManager.closeConnection();
                LOGGER.info("Database connection manager closed");
            } catch (Exception e) {
                LOGGER.warning("Error closing database connection manager: " + e.getMessage());
            }

            // Close data sources
            try {
                if (ineDataSource != null) {
                    ineDataSource.close();
                    LOGGER.info("INE data source closed");
                }
            } catch (Exception e) {
                LOGGER.warning("Error closing INE data source: " + e.getMessage());
            }

            try {
                if (bormeDataSource != null) {
                    bormeDataSource.close();
                    LOGGER.info("BORME data source closed");
                }
            } catch (Exception e) {
                LOGGER.warning("Error closing BORME data source: " + e.getMessage());
            }
        } finally {
            // Release the shutdown latch
            shutdownLatch.countDown();
            LOGGER.info("Service shutdown complete");
        }
    }
}