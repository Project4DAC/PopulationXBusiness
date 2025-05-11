package org.ulpgc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageBrokerConnector;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageSaver;
import org.ulpgc.BormeSubscriberDataLake.processors.BormeDataProcessor;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataProcessor;
import org.ulpgc.BormeSubscriberDataLake.services.*;

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
        Config config = readConfig();
        setupDataLake();

        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;
        MessageBrokerConnector connector = new ActiveMQConnector(config);
        MessageSaver messageSaver = new FileMessageSaver(rawPath);

        MessageProcessor processor = new MessageProcessor(connector, messageSaver);
        processor.startProcessing();

        // Initialize the DataProcessor for handling zones
        DataProcessor dataProcessor = new BormeDataProcessor(DATALAKE_ROOT);

        // Set up scheduled processing task
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Starting automated data processing...");
            dataProcessor.process();
            System.out.println("Automated data processing complete.");
        }, 1, PROCESSING_INTERVAL_MINUTES, TimeUnit.MINUTES);

        System.out.println("Automated data processing scheduled every " +
                PROCESSING_INTERVAL_MINUTES + " minutes.");

        CommandLineInterface cli = new CommandLineInterface(processor, dataProcessor);
        cli.start();

        // Clean up when CLI exits
        scheduler.shutdown();
    }

    private static Config readConfig() {
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