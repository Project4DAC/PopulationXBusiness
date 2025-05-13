package org.ulpgc.IneSubscriberDataMart.Interfaces;

import java.util.function.Consumer;

public interface MessageBrokerConnector {
    void connect() throws Exception;
    void disconnect() throws Exception;
    void setMessageHandler(Consumer<String> messageHandler);
}