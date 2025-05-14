package org.ulpgc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataProcessor;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageBrokerConnector;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageSaver;
import org.ulpgc.IneSubscriberDataMart.services.*;

import javax.sql.DataSource;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {

    private static DataSource ineDataSource;

    public static class DatabaseUtil {
        public static HikariDataSource createDataSource(String dbPath, String poolName) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setDriverClassName("org.sqlite.JDBC");
            config.setMaximumPoolSize(1);
            config.setPoolName(poolName);
            return new HikariDataSource(config);

        }
    }
    ineDataSource = DatabaseUtil.createDataSource("./INE.db", "INEPool");
    INETableCommandFactory.createInitializeDatabaseCommand(ineDataSource).execute();


    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static MessageProcessor messageProcessor;
    private static DataProcessor dataProcessor;

    public static void main(String[] args) {
        configureLogging();

        LOGGER.info("Starting INE Subscriber Data Mart Background Service...");

        // Initialize configuration
        String username = getConfigValue("ACTIVEMQ_USERNAME", "admin");
        String password = getConfigValue("ACTIVEMQ_PASSWORD", "admin");
        String brokerUrl = getConfigValue("ACTIVEMQ_URL", "tcp://localhost:61616");
        List<String> topicNames = Arrays.asList(
                "INE.DATOS_TABLA",
                "INE.METADATOS",
                "INE.OPERACIONES",
                "INE.TABLAS_OPERACIONES"
        );

        // Setup data paths
        String dataMartRoot = getConfigValue("DATAMART_PATH",
                System.getProperty("user.home") + File.separator + "activemq-datamart");

        // Initialize components
        Config config = new Config(username, password, brokerUrl, topicNames);
        MessageBrokerConnector brokerConnector = new ActiveMQConnector(config);
        MessageSaver messageSaver = new FileMessageSaver(dataMartRoot + File.separator + "staging");
        messageProcessor = new MessageProcessor(brokerConnector, messageSaver);
        dataProcessor = new INEDataMartProcessor(dataMartRoot);

        // Start message processing
        messageProcessor.startProcessing();
        LOGGER.info("Message processor started successfully. Listening for INE messages...");

        // Schedule automatic data processing every 6 hours
        setupAutomaticProcessing();

        // Setup shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Service shutdown initiated...");
            if (messageProcessor.isRunning()) {
                messageProcessor.stopProcessing();
                LOGGER.info("Message processor stopped");
            }
            shutdownLatch.countDown();
            LOGGER.info("Service shutdown complete");
        }));

        // Keep the service running
        try {
            shutdownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.warning("Service interrupted: " + e.getMessage());
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
        Thread processingThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Sleep for 6 hours
                    Thread.sleep(6 * 60 * 60 * 1000);

                    // Process the data
                    LOGGER.info("Starting scheduled data processing...");
                    dataProcessor.process();
                    LOGGER.info("Scheduled data processing completed");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.info("Automatic processing thread interrupted");
                    break;
                }
            }
        });
        processingThread.setDaemon(true);
        processingThread.start();
        LOGGER.info("Automatic data processing scheduled every 6 hours");
    }

    private static String getConfigValue(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

}