package org.ulpgc.BormeSubscriberDataLake.services;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageBrokerConnector;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageSaver;

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
                System.out.println("Received message: " + message);
                messageSaver.saveMessage(message);
            });
            
            System.out.println("Message processor started. Waiting for messages...");
            isRunning = true;
        } catch (Exception e) {
            System.err.println("Error starting message processor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopProcessing() {
        try {
            brokerConnector.disconnect();
            isRunning = false;
            System.out.println("Message processor stopped");
        } catch (Exception e) {
            System.err.println("Error stopping message processor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}