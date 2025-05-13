package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageBrokerConnector;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageSaver;

public class MessageProcessor {
    private final MessageBrokerConnector brokerConnector;
    private final MessageSaver messageSaver;
    private boolean isRunning = false;

    public MessageProcessor(MessageBrokerConnector brokerConnector, MessageSaver messageSaver) {
        this.brokerConnector = brokerConnector;
        this.messageSaver = messageSaver;
    }

    public void startProcessing() {
        try {
            brokerConnector.connect();
            brokerConnector.setMessageHandler(message -> {
                System.out.println("Received INE message: " + message);
                messageSaver.saveMessage(message);
            });
            
            System.out.println("INE message processor started. Waiting for messages...");
            isRunning = true;
        } catch (Exception e) {
            System.err.println("Error starting INE message processor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopProcessing() {
        try {
            brokerConnector.disconnect();
            isRunning = false;
            System.out.println("INE message processor stopped");
        } catch (Exception e) {
            System.err.println("Error stopping INE message processor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}