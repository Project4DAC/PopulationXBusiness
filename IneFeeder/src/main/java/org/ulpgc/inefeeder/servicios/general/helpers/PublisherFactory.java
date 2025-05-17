package org.ulpgc.inefeeder.servicios.general.helpers;


import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;

public class PublisherFactory {
    /**
     * Creates an ActiveMQ publisher with default broker URL (localhost:61616)
     * @return a new ActiveMQ publisher instance
     */
    public static Publisher createActiveMQPublisher() {
        return new ActiveMQPublisher("tcp://localhost:61616");
    }
    
    /**
     * Creates an ActiveMQ publisher with a specified broker URL
     * @param brokerUrl the URL of the ActiveMQ broker
     * @return a new ActiveMQ publisher instance
     */
    public static Publisher createActiveMQPublisher(String brokerUrl) {
        return new ActiveMQPublisher(brokerUrl);
    }
}