package org.ulpgc.inefeeder.servicios;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;


/**
 * Implementation of Publisher interface for ActiveMQ message broker
 */
public class ActiveMQPublisher implements Publisher {
    private final String brokerUrl;
    private Connection connection;
    private Session session;
    private boolean connected;

    /**
     * Creates a new ActiveMQ publisher with the specified broker URL
     * @param brokerUrl the URL of the ActiveMQ broker (e.g. "tcp://localhost:61616")
     */
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
        
        Destination dest = session.createQueue(destination);
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