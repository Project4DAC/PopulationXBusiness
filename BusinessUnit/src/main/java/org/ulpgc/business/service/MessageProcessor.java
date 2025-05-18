//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.business.service;

import org.ulpgc.business.interfaces.MessageBrokerConnector;
import org.ulpgc.business.interfaces.MessageSaver;

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
            this.brokerConnector.connect();
            this.brokerConnector.setMessageHandler((message) -> {
                System.out.println("Received message: " + message);
                this.messageSaver.saveMessage(message);
            });
            System.out.println("Message processor started. Waiting for messages...");
            this.isRunning = true;
        } catch (Exception var2) {
            Exception e = var2;
            System.err.println("Error starting message processor: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void stopProcessing() {
        try {
            this.brokerConnector.disconnect();
            this.isRunning = false;
            System.out.println("Message processor stopped");
        } catch (Exception var2) {
            Exception e = var2;
            System.err.println("Error stopping message processor: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
