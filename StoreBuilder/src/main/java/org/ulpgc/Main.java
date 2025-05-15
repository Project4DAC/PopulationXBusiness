package org.ulpgc;

import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;
import org.ulpgc.StoreBuilder.Interfaces.MessageBrokerConnector;
import org.ulpgc.StoreBuilder.Interfaces.MessageSaver;
import org.ulpgc.StoreBuilder.processors.BormeDataProcessor;
import org.ulpgc.StoreBuilder.processors.INEDataProcessor;
import org.ulpgc.StoreBuilder.services.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final String DATALAKE_ROOT = System.getenv("DATALAKE_PATH") != null ?
            System.getenv("DATALAKE_PATH") :
            System.getProperty("user.home") + File.separator + "activemq-datalake";

    private static final String RAW_ZONE = "raw";
    private static final String PROCESSED_ZONE = "processed";
    private static final String CONSUMPTION_ZONE = "consumption";

    private static final int PROCESSING_INTERVAL_MINUTES = 5;

    // Shared maps for CLI
    private static final Map<String, MessageProcessor> processors = new HashMap<>();
    private static final Map<String, DataProcessor> dataProcessors = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Starting Combined BORME/INE Data Lake Application...");

        setupDataLake();

        startBormeComponents();
        startINEComponents();

        startCLI();
    }

    private static void startBormeComponents() {
        Config bormeConfig = readBormeConfig();
        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;

        MessageBrokerConnector bormeConnector = new ActiveMQConnector(bormeConfig);
        MessageSaver bormeMessageSaver = new FileMessageSaver(rawPath);
        MessageProcessor bormeProcessor = new MessageProcessor(bormeConnector, bormeMessageSaver);
        DataProcessor bormeDataProcessor = new BormeDataProcessor(DATALAKE_ROOT);

        // Store references for CLI
        processors.put("borme", bormeProcessor);
        dataProcessors.put("borme", bormeDataProcessor);

        // Schedule BORME processor
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Starting automated BORME data processing...");
            bormeDataProcessor.process();
            System.out.println("Automated BORME data processing complete.");
        }, 1, PROCESSING_INTERVAL_MINUTES, TimeUnit.MINUTES);

        bormeProcessor.startProcessing();

        System.out.println("BORME data processing scheduled every " + PROCESSING_INTERVAL_MINUTES + " minutes.");
    }


    private static void startINEComponents() {
        Config ineConfig = readINEConfig();
        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;

        MessageBrokerConnector ineConnector = new ActiveMQConnector(ineConfig);
        MessageSaver ineMessageSaver = new FileMessageSaver(rawPath);
        MessageProcessor ineProcessor = new MessageProcessor(ineConnector, ineMessageSaver);
        DataProcessor ineDataProcessor = new INEDataProcessor(DATALAKE_ROOT);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Starting automated INE data processing...");
            ineDataProcessor.process();
            System.out.println("Automated INE data processing complete.");
        }, 1, PROCESSING_INTERVAL_MINUTES, TimeUnit.MINUTES);

        ineProcessor.startProcessing();

        System.out.println("INE data processing scheduled every " + PROCESSING_INTERVAL_MINUTES + " minutes.");
    }

    private static void startCLI() {
        CommandLineInterface cli = new CommandLineInterface(processors, dataProcessors);
        cli.start();
    }

    private static Config readBormeConfig() {
        String user = System.getenv("ACTIVEMQ_USER");
        String password = System.getenv("ACTIVEMQ_PASSWORD");
        String brokerUrl = System.getenv("ACTIVEMQ_BROKER_URL");
        String topicName = System.getenv("ACTIVEMQ_TOPIC");

        if (brokerUrl == null) brokerUrl = "tcp://localhost:61616";
        if (topicName == null) topicName = "BORME.Publicaciones.Nuevas";

        if (user == null || password == null) {
            throw new RuntimeException("Environment variables ACTIVEMQ_USER and ACTIVEMQ_PASSWORD must be set");
        }

        return new Config(user, password, brokerUrl, Collections.singletonList(topicName));
    }

    private static Config readINEConfig() {
        String username = System.getenv("ACTIVEMQ_USER") != null ?
                System.getenv("ACTIVEMQ_USER") : "admin";
        String password = System.getenv("ACTIVEMQ_PASSWORD") != null ?
                System.getenv("ACTIVEMQ_PASSWORD") : "admin";
        String brokerUrl = System.getenv("ACTIVEMQ_BROKER_URL") != null ?
                System.getenv("ACTIVEMQ_BROKER_URL") : "tcp://localhost:61616";

        List<String> topicNames = Arrays.asList(
                "INE.DATOS_TABLA",
                "INE.METADATOS",
                "INE.OPERACIONES",
                "INE.TABLAS_OPERACIONES"
        );

        return new Config(username, password, brokerUrl, topicNames);
    }

    private static void setupDataLake() {
        try {
            Path datalakePath = Paths.get(DATALAKE_ROOT);
            if (!Files.exists(datalakePath)) {
                Files.createDirectory(datalakePath);
                System.out.println("Created DataLake root directory: " + datalakePath);
            }

            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, RAW_ZONE));
            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, PROCESSED_ZONE));
            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, CONSUMPTION_ZONE));

            System.out.println("DataLake structure initialized successfully");
        } catch (IOException e) {
            System.err.println("Error creating DataLake structure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            System.out.println("Created directory: " + path);
        }
    }
}
