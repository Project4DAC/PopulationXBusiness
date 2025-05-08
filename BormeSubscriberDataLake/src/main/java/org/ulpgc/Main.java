package org.ulpgc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageBrokerConnector;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageSaver;
import org.ulpgc.BormeSubscriberDataLake.services.*;

public class Main {
    private static final String DATALAKE_ROOT = System.getenv("DATALAKE_PATH") != null ?
            System.getenv("DATALAKE_PATH") :
            System.getProperty("user.home") + File.separator + "activemq-datalake";
    private static final String RAW_ZONE = "raw";
    private static final String PROCESSED_ZONE = "processed";
    private static final String CONSUMPTION_ZONE = "consumption";

    public static void main(String[] args) {
        Config config = readConfig();
        setupDataLake();

        String rawPath = DATALAKE_ROOT + File.separator + RAW_ZONE;
        MessageBrokerConnector connector = new ActiveMQConnector(config);
        MessageSaver messageSaver = new FileMessageSaver(rawPath);

        MessageProcessor processor = new MessageProcessor(connector, messageSaver);
        processor.startProcessing();

        CommandLineInterface cli = new CommandLineInterface(processor);
        cli.start();
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

