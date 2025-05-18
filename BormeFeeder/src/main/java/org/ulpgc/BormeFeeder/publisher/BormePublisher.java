package org.ulpgc.BormeFeeder.publisher;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.BormeFeeder.commands.query.BuildURLCommand;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;
import org.ulpgc.BormeFeeder.services.general.helpers.SimpleInput;
import org.ulpgc.BormeFeeder.services.general.helpers.SimpleOutput;

import jakarta.jms.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BormePublisher {
    private final String brokerUrl;
    private final String user;
    private final String password;
    private final String topicName = "BORME.New.Publications";
    private static final boolean DEBUG_MODE = true;

    public BormePublisher() {
        // Load connection details with detailed logging
        log("Initializing BORME Publisher");


            log("ACTIVEMQ_BROKER_URL not set, defaulting to localhost");
            this.brokerUrl = "tcp://localhost:61616";

        this.user = System.getenv("ACTIVEMQ_USER");
        this.password = System.getenv("ACTIVEMQ_PASSWORD");

        if (user == null || password == null || user.isEmpty() || password.isEmpty()) {
            String errorMsg = "Environment variables ACTIVEMQ_USER and ACTIVEMQ_PASSWORD must be set";
            logError(errorMsg);
            throw new RuntimeException(errorMsg);
        } else {
            log("Authentication credentials found for user: " + user);
        }

        log("Topic name: " + topicName);
    }

    public void publishLatest() {
        log("Starting to search for latest BORME publication...");
        boolean found = false;

        for (int daysBack = 0; daysBack <= 5; daysBack++) {
            LocalDate targetDate = LocalDate.now().minusDays(daysBack);
            log("Checking for publication on: " + targetDate);

            if (targetDate.getDayOfWeek().getValue() > 5) {
                log("Skipping weekend day: " + targetDate.getDayOfWeek());
                continue;
            }

            try {
                log("Attempting to build URL for date: " + targetDate);
                Input input = new SimpleInput();
                Output output = new SimpleOutput();

                input.setValue("date", targetDate.toString());
                log("Executing BuildURLCommand for date: " + targetDate);

                BuildURLCommand buildURLCommand = new BuildURLCommand(input, output);
                buildURLCommand.execute();

                String url = output.getValue("url");
                log("Command executed, resulting URL: " + url);

                if (url != null && !url.isEmpty()) {
                    log("Valid URL found for date: " + targetDate + ", URL: " + url);

                    try {
                        publishNewBormeMessage(targetDate, url);
                        found = true;
                        break; // Exit the loop after successful publication
                    } catch (Exception e) {
                        logError("Failed to publish message: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    log("No valid URL found for date: " + targetDate);
                }
            } catch (Exception e) {
                logError("Error processing date " + targetDate + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (!found) {
            logError("Failed to find any valid BORME publications for recent dates");
        }
    }

    public void publishMessage(LocalDate date, String url) {
        log("Manual publish request for date: " + date + ", URL: " + url);
        publishNewBormeMessage(date, url);
    }

    // Test method to check ActiveMQ connectivity only, without actual publication
    public boolean testConnection() {
        log("Testing connection to ActiveMQ broker: " + brokerUrl);
        Connection connection = null;

        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connectionFactory.setUserName(user);
            connectionFactory.setPassword(password);
            connection = connectionFactory.createConnection();
            connection.start();
            log("Connection to ActiveMQ successful!");
            return true;
        } catch (JMSException e) {
            logError("Connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    log("Test connection closed");
                } catch (JMSException e) {
                    logError("Error closing test connection: " + e.getMessage());
                }
            }
        }
    }

    private void publishNewBormeMessage(LocalDate date, String url) {
        log("Preparing to publish message for date: " + date);
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try {
            log("Creating connection factory with URL: " + brokerUrl);
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connectionFactory.setUserName(user);
            connectionFactory.setPassword(password);

            log("Establishing connection to ActiveMQ...");
            connection = connectionFactory.createConnection();
            connection.start();
            log("Connected to ActiveMQ!");

            log("Creating session...");
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            log("Session created successfully!");

            log("Accessing topic: " + topicName);
            Topic topic = session.createTopic(topicName);
            log("Topic accessed: " + topicName);

            log("Creating producer...");
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            log("Producer created with PERSISTENT delivery mode");

            String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String messageContent = String.format(
                    "{ \"message\": \"New publication in the BORME!\", " +
                            "\"date\": \"%s\", " +
                            "\"url\": \"%s\" }",
                    formattedDate, url);

            log("Creating message with content: " + messageContent);
            TextMessage message = session.createTextMessage(messageContent);

            log("Sending message to topic...");
            producer.send(message);
            log("Message sent successfully to topic: " + topicName);

        } catch (JMSException e) {
            logError("JMS Exception during message publication: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            logError("Unexpected error during message publication: " + e.getMessage());
            e.printStackTrace();
        } finally {
            log("Cleaning up resources...");
            try {
                if (producer != null) {
                    producer.close();
                    log("Producer closed");
                }
                if (session != null) {
                    session.close();
                    log("Session closed");
                }
                if (connection != null) {
                    connection.close();
                    log("Connection closed");
                }
            } catch (JMSException e) {
                logError("Error during cleanup: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Simple logging methods
    private void log(String message) {
        if (DEBUG_MODE) {
            System.out.println("[BORME-PUBLISHER] " + message);
        }
    }

    private void logError(String message) {
        System.err.println("[BORME-PUBLISHER ERROR] " + message);
    }

    // A main method for testing the publisher directly
    public static void main(String[] args) {
        BormePublisher publisher = new BormePublisher();

        // First test connection only
        if (publisher.testConnection()) {
            System.out.println("Connection test successful, attempting to publish latest BORME...");
            publisher.publishLatest();
        } else {
            System.err.println("Connection test failed, cannot proceed with publication");
        }
    }
}