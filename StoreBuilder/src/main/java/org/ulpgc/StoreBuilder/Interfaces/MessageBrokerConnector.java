//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.Interfaces;

import java.util.function.Consumer;

public interface MessageBrokerConnector {
    void connect() throws Exception;

    void disconnect() throws Exception;

    void setMessageHandler(Consumer<String> var1);
}
