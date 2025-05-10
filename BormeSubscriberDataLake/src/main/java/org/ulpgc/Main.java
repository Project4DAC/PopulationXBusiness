package org.ulpgc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageBrokerConnector;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageSaver;
import org.ulpgc.BormeSubscriberDataLake.processors.BormeDataProcessor;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataProcessor;
import org.ulpgc.BormeSubscriberDataLake.services.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String DATALAKE_ROOT = System.getenv("DATALAKE_PATH") != null ?
            System.getenv("DATALAKE_PATH") :
            System.getProperty("user.home") + File.separator + "activemq-datalake";
    private static final String RAW_ZONE = "raw";
    private static final String PROCESSED_ZONE = "processed";
    private static final String CONSUMPTION_ZONE = "consumption";

    public static void main(String[] args) {
        try {
            logger.info("Starting BormeSubscriberDataLake application");

            // Read configuration
            Config config = readConfig();
            logger.info("Configuration loaded successfully");

            // Setup DataLake directory structure
            setupDataLake();

            // Initialize components
            String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;
            MessageBrokerConnector connector = new ActiveMQConnector(config);
            MessageSaver messageSaver = new FileMessageSaver(rawPath);

            // Initialize the processor
            MessageProcessor processor = new MessageProcessor(connector, messageSaver);

            // Start message processing
            try {
                processor.startProcessing();
                logger.info("Message processor started successfully");
            } catch (Exception e) {
                logger.error("Failed to start message processor", e);
                System.err.println("Failed to connect to message broker. Check your connection settings and credentials.");
                System.err.println("Error: " + e.getMessage());

                // Continue with CLI even if broker connection fails
                System.out.println("Continuing with command-line interface...");
            }

            // Initialize the DataProcessor for handling zones
            DataProcessor dataProcessor = new BormeDataProcessor(DATALAKE_ROOT);
            logger.info("Data processor initialized");

            // Start the command-line interface
            CommandLineInterface cli = new CommandLineInterface(processor, dataProcessor);
            logger.info("Starting command-line interface");
            cli.start();

        } catch (Exception e) {
            logger.error("Application startup failed", e);
            System.err.println("Fatal error during application startup: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Config readConfig() {
        String user = System.getenv("ACTIVEMQ_USER");
        String password = System.getenv("ACTIVEMQ_PASSWORD");
        String brokerUrl = System.getenv("ACTIVEMQ_BROKER_URL");
        String topicName = System.getenv("ACTIVEMQ_TOPIC");

        if (brokerUrl == null) {
            brokerUrl = "tcp://localhost:61616";
            logger.info("Using default broker URL: {}", brokerUrl);
        }

        if (topicName == null) {
            topicName = "BORME.Publicaciones.Nuevas";
            logger.info("Using default topic name: {}", topicName);
        }

        if (user == null || password == null) {
            logger.error("Missing ActiveMQ credentials");
            throw new RuntimeException("Environment variables ACTIVEMQ_USER and ACTIVEMQ_PASSWORD must be set");
        }

        return new Config(user, password, brokerUrl, topicName);
    }

    private static void setupDataLake() {
        try {
            Path datalakePath = Paths.get(DATALAKE_ROOT);
            if (!Files.exists(datalakePath)) {
                Files.createDirectory(datalakePath);
                logger.info("Created DataLake root directory: {}", datalakePath);
            }

            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, RAW_ZONE));
            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, PROCESSED_ZONE));
            createDirectoryIfNotExists(Paths.get(DATALAKE_ROOT, CONSUMPTION_ZONE));

            logger.info("DataLake structure initialized successfully");
        } catch (IOException e) {
            logger.error("Error creating DataLake structure", e);
            throw new RuntimeException("Failed to create DataLake directory structure: " + e.getMessage(), e);
        }
    }

    private static void createDirectoryIfNotExists(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
            logger.info("Created directory: {}", path);
        }
    }
}