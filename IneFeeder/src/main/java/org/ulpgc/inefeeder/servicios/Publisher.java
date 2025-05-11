package org.ulpgc.inefeeder.servicios;

/**
 * Interface for message publishing services like ActiveMQ
 */
public interface Publisher {
    /**
     * Publishes a message to a specific destination
     * @param destination the destination to publish to (e.g., queue or topic name)
     * @param message the message content to publish
     * @throws Exception if there's an error publishing the message
     */
    void publish(String destination, String message) throws Exception;
    
    /**
     * Close connection and resources
     */
    void close();
    
    /**
     * Checks if the publisher is connected
     * @return true if connected, false otherwise
     */
    boolean isConnected();
}