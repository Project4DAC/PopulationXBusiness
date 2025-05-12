package org.ulpgc.inefeeder.servicios.general.Interfaces;


public interface Publisher {

    void publish(String destination, String message) throws Exception;
    

    void close();

    boolean isConnected();
}