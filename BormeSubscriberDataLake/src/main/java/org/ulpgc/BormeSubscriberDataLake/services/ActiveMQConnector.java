package org.ulpgc.BormeSubscriberDataLake.services;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.MessageBrokerConnector;

import jakarta.jms.Connection;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;

import java.util.function.Consumer;

public class ActiveMQConnector implements MessageBrokerConnector {
    private final Config config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public ActiveMQConnector(Config config) {
        this.config = config;
    }

    @Override
    public void connect() throws Exception {
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(config.getBrokerUrl());
        connectionFactory.setUserName(config.getUsername());
        connectionFactory.setPassword(config.getPassword());

        connection = connectionFactory.createConnection();
        connection.start();
        System.out.println("Connected to ActiveMQ!");

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        System.out.println("Session created successfully!");

        Topic topic = session.createTopic(config.getTopicName());
        System.out.println("Subscribed to topic: " + config.getTopicName());

        consumer = session.createConsumer(topic);
    }

    @Override
    public void setMessageHandler(Consumer<String> messageHandler) {
        try {
            consumer.setMessageListener(message -> {
                if (message instanceof TextMessage) {
                    try {
                        String text = ((TextMessage) message).getText();
                        messageHandler.accept(text);
                    } catch (jakarta.jms.JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (jakarta.jms.JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() throws Exception {
        if (consumer != null) consumer.close();
        if (session != null) session.close();
        if (connection != null) connection.close();
    }
}