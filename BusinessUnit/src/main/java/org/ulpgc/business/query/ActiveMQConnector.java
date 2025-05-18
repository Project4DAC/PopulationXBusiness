//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.business.query;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import java.util.function.Consumer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.business.interfaces.MessageBrokerConnector;

public class ActiveMQConnector implements MessageBrokerConnector {
    private final Config config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public ActiveMQConnector(Config config) {
        this.config = config;
    }

    public void connect() throws Exception {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(this.config.getBrokerUrl());
        connectionFactory.setUserName(this.config.getUsername());
        connectionFactory.setPassword(this.config.getPassword());
        this.connection = connectionFactory.createConnection();
        this.connection.start();
        System.out.println("Connected to ActiveMQ!");
        this.session = this.connection.createSession(false, 1);
        System.out.println("Session created successfully!");
        Topic topic = this.session.createTopic(this.config.getTopicName());
        System.out.println("Subscribed to topic: " + this.config.getTopicName());
        this.consumer = this.session.createConsumer(topic);
    }

    public void setMessageHandler(Consumer<String> messageHandler) {
        try {
            this.consumer.setMessageListener((message) -> {
                if (message instanceof TextMessage) {
                    try {
                        String text = ((TextMessage)message).getText();
                        messageHandler.accept(text);
                    } catch (JMSException var3) {
                        JMSException e = var3;
                        e.printStackTrace();
                    }
                }

            });
        } catch (JMSException var3) {
            JMSException e = var3;
            e.printStackTrace();
        }

    }

    public void disconnect() throws Exception {
        if (this.consumer != null) {
            this.consumer.close();
        }

        if (this.session != null) {
            this.session.close();
        }

        if (this.connection != null) {
            this.connection.close();
        }

    }
}
