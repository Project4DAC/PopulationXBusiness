package org.ulpgc;

import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;
import org.ulpgc.StoreBuilder.Interfaces.MessageBrokerConnector;
import org.ulpgc.StoreBuilder.Interfaces.MessageSaver;
import org.ulpgc.StoreBuilder.processors.BormeDataProcessor;
import org.ulpgc.StoreBuilder.services.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String DATALAKE_ROOT = System.getenv("DATALAKE_PATH") != null ?
            System.getenv("DATALAKE_PATH") :
            System.getProperty("user.home") + File.separator + "activemq-datalake";
    private static final String RAW_ZONE = "raw";
    private static final String PROCESSED_ZONE = "processed";
    private static final String CONSUMPTION_ZONE = "consumption";

    // Configure the processing interval (in minutes)
    private static final int PROCESSING_INTERVAL_MINUTES = 5;

    public static void main(String[] args) {
        System.out.println("Starting Combined BORME/INE Data Lake Application...");

        // Setup the DataLake structure
        setupDataLake();

        // Initialize and start BORME components
        startBormeComponents();

        // Initialize and start INE components
        startINEComponents();

        // Start the combined CLI
        startCLI();
    }

    private static void startBormeComponents() {
        Config bormeConfig = readBormeConfig();
        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;

        MessageBrokerConnector bormeConnector = new ActiveMQConnector(bormeConfig);
        MessageSaver bormeMessageSaver = new FileMessageSaver(rawPath);
        MessageProcessor bormeProcessor =
                new MessageProcessor(bormeConnector, bormeMessageSaver);

        DataProcessor bormeDataProcessor =
                new BormeDataProcessor(DATALAKE_ROOT);

        // Set up scheduled processing task for BORME
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Starting automated BORME data processing...");
            bormeDataProcessor.process();
            System.out.println("Automated BORME data processing complete.");
        }, 1, PROCESSING_INTERVAL_MINUTES, TimeUnit.MINUTES);

        System.out.println("BORME data processing scheduled every " +
                PROCESSING_INTERVAL_MINUTES + " minutes.");

        bormeProcessor.startProcessing();

    }

    private static void startINEComponents() {
        Config ineConfig = readINEConfig();
        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;

        // INE specific components
        MessageBrokerConnector ineConnector = new ActiveMQConnector(ineConfig);
        MessageSaver ineMessageSaver = new FileMessageSaver(rawPath);
        MessageProcessor ineProcessor =
                new MessageProcessor(ineConnector, ineMessageSaver);

        // Initialize the DataProcessor for handling zones
        DataProcessor ineDataProcessor =
                new INEDataProcessor(DATALAKE_ROOT);

        // Start processing incoming messages
        ineProcessor.startProcessing();
    }

    private static void startCLI() {
        CommandLineInterface cli = new CommandLineInterface(messageProcessor, dataProcessor);
        cli.start();
    }
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

        return new Config(user, password, brokerUrl, topicName);
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
