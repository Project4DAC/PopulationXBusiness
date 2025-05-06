package org.ulpgc;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;

public class Main {
    public static void main(String[] args) {
        String user = System.getenv("ACTIVEMQ_USER");
        String password = System.getenv("ACTIVEMQ_PASSWORD");
        String brokerUrl = "tcp://localhost:61616";

        if (user == null || password == null) {
            throw new RuntimeException("Environment variables ACTIVEMQ_USER and ACTIVEMQ_PASSWORD must be set");
        }

        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);

            connectionFactory.setUserName(user);
            connectionFactory.setPassword(password);

            connection = connectionFactory.createConnection();
            connection.start();
            System.out.println("Connected to ActiveMQ!");

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            System.out.println("Session created successfully!");

            String topicName = "BORME.Publicaciones.Nuevas";
            Topic topic = session.createTopic(topicName);
            System.out.println("Subscribed to topic: " + topicName);

            consumer = session.createConsumer(topic);

            consumer.setMessageListener(new MessageListener() {
                @Override
                public void onMessage(Message message) {
                    if (message instanceof TextMessage) {
                        try {
                            String text = ((TextMessage) message).getText();
                            System.out.println("Received message: " + text);
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            System.out.println("Waiting for messages...");
            Thread.sleep(1000000);

        } catch (JMSException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                if (consumer != null) consumer.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}