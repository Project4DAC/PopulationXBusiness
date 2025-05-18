package org.ulpgc.business.interfaces;

import java.util.function.Consumer;

public interface MessageBrokerConnector {
    void connect() throws Exception;

    void disconnect() throws Exception;

    void setMessageHandler(Consumer<String> var1);
}
