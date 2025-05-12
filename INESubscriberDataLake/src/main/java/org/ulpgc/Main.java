package org.ulpgc;

import org.ulpgc.INESubscriberDataLake.services.CLI;
import org.ulpgc.INESubscriberDataLake.Interfaces.DataProcessor;
import org.ulpgc.INESubscriberDataLake.Interfaces.MessageBrokerConnector;
import org.ulpgc.INESubscriberDataLake.Interfaces.MessageSaver;
import org.ulpgc.INESubscriberDataLake.services.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting INE Subscriber Data Lake Application...");

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
        String dataLakeRoot = System.getenv("DATALAKE_PATH") != null ?
                System.getenv("DATALAKE_PATH") :
                System.getProperty("user.home") + File.separator + "activemq-datalake";
        ensureDirectoriesExist(dataLakeRoot);

        // Initialize components
        Config config = new Config(username, password, brokerUrl, topicNames);
        MessageBrokerConnector brokerConnector = new ActiveMQConnector(config);
        MessageSaver messageSaver = new FileMessageSaver(dataLakeRoot + File.separator + "raw");
        MessageProcessor messageProcessor = new MessageProcessor(brokerConnector, messageSaver);
        DataProcessor dataProcessor = new INEDataProcessor(dataLakeRoot);

        // Start message processing
        messageProcessor.startProcessing();

        // Start CLI
        CLI cli = new CLI(messageProcessor, dataProcessor);
        cli.start();
    }

    private static void ensureDirectoriesExist(String dataLakeRoot) {
        // Create main datalake directory
        new File(dataLakeRoot).mkdirs();

        // Create zone directories
        new File(dataLakeRoot + File.separator + "raw").mkdirs();
        new File(dataLakeRoot + File.separator + "processed").mkdirs();
        new File(dataLakeRoot + File.separator + "consumption").mkdirs();

        System.out.println("DataLake directories ensured at: " + dataLakeRoot);
    }
}