package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageBrokerConnector;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageSaver;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageProcessor {
    private static final Logger LOGGER = Logger.getLogger(MessageProcessor.class.getName());
    private final MessageBrokerConnector brokerConnector;
    private final MessageSaver messageSaver;
    private boolean isRunning = false;
    private int messageCount = 0;
    private long lastStatusLogTime = 0;
    private static final long STATUS_LOG_INTERVAL = 3600000; // 1 hour in milliseconds

    public MessageProcessor(MessageBrokerConnector brokerConnector, MessageSaver messageSaver) {
        this.brokerConnector = brokerConnector;
        this.messageSaver = messageSaver;
    }

    public void startProcessing() {
        try {
            brokerConnector.connect();
            brokerConnector.setMessageHandler(message -> {
                messageCount++;
                LOGGER.info("Received INE message #" + messageCount);
                LOGGER.fine("Message content: " + message);

                // Log detailed status periodically
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastStatusLogTime > STATUS_LOG_INTERVAL) {
                    LOGGER.info("Status update: Processed " + messageCount + " messages since startup");
                    lastStatusLogTime = currentTime;
                }

                try {
                    messageSaver.saveMessage(message);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error saving message: " + e.getMessage(), e);
                }
            });

            LOGGER.info("INE message processor started. Waiting for messages...");
            isRunning = true;
            lastStatusLogTime = System.currentTimeMillis();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting INE message processor: " + e.getMessage(), e);
        }
    }

    public void stopProcessing() {
        try {
            brokerConnector.disconnect();
            isRunning = false;
            LOGGER.info("INE message processor stopped. Total messages processed: " + messageCount);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error stopping INE message processor: " + e.getMessage(), e);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getMessageCount() {
        return messageCount;
    }
}