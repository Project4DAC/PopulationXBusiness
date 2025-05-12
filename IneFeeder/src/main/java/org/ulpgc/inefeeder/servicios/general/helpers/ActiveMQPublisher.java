package org.ulpgc.inefeeder.servicios.general.helpers;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Publisher;


/**
 * Implementation of Publisher interface for ActiveMQ message broker
 */
public class ActiveMQPublisher implements Publisher {
    private final String brokerUrl;
    private Connection connection;
    private Session session;
    private boolean connected;

    public ActiveMQPublisher(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.connected = false;
    }

    public void connect() throws JMSException {
        if (!connected) {
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connected = true;
        }
    }

    @Override
    public void publish(String destination, String message) throws Exception {
        if (!connected) {
            connect();
        }
        
        Destination dest = session.createTopic(destination);
        MessageProducer producer = session.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        
        TextMessage textMessage = session.createTextMessage(message);
        producer.send(textMessage);
        producer.close();
    }

    @Override
    public void close() {
        if (connected) {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            } finally {
                connected = false;
            }
        }
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}