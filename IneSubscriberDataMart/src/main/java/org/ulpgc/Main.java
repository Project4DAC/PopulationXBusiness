package org.ulpgc;

import org.ulpgc.IneSubscriberDataMart.services.CLI;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataProcessor;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageBrokerConnector;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageSaver;
import org.ulpgc.IneSubscriberDataMart.services.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting INE Subscriber Data Mart Application...");

        String username = "admin";
        String password = "admin";
        String brokerUrl = "tcp://localhost:61616";
        List<String> topicNames = Arrays.asList(
                "INE.DATOS_TABLA",
                "INE.METADATOS",
                "INE.OPERACIONES",
                "INE.TABLAS_OPERACIONES"
        );

        // Setup data paths
        String dataMartRoot = System.getenv("DATAMART_PATH") != null ?
                System.getenv("DATAMART_PATH") :
                System.getProperty("user.home") + File.separator + "activemq-datamart";
        ensureDirectoriesExist(dataMartRoot);

        // Initialize components
        Config config = new Config(username, password, brokerUrl, topicNames);
        MessageBrokerConnector brokerConnector = new ActiveMQConnector(config);
        MessageSaver messageSaver = new FileMessageSaver(dataMartRoot + File.separator + "staging");
        MessageProcessor messageProcessor = new MessageProcessor(brokerConnector, messageSaver);
        DataProcessor dataProcessor = new INEDataMartProcessor(dataMartRoot);

        // Start message processing
        messageProcessor.startProcessing();

        // Start CLI
        CLI cli = new CLI(messageProcessor, dataProcessor);
        cli.start();
    }

    private static void ensureDirectoriesExist(String dataMartRoot) {
        // Create main datamart directory
        new File(dataMartRoot).mkdirs();

        // Create datamart directories
        new File(dataMartRoot + File.separator + "staging").mkdirs();
        new File(dataMartRoot + File.separator + "dimensions").mkdirs();
        new File(dataMartRoot + File.separator + "facts").mkdirs();
        new File(dataMartRoot + File.separator + "reports").mkdirs();

        System.out.println("Data Mart directories ensured at: " + dataMartRoot);
    }
}