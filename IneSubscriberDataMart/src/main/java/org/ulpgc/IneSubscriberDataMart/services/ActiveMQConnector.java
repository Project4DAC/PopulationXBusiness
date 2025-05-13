package org.ulpgc.IneSubscriberDataMart.services;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.IneSubscriberDataMart.Interfaces.MessageBrokerConnector;
import org.ulpgc.IneSubscriberDataMart.services.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ActiveMQConnector implements MessageBrokerConnector {
    private final Config config;
    private Connection connection;
    private Session session;
    private MessageConsumer consumer;

    public ActiveMQConnector(Config config) {
        this.config = config;
    }

    private List<MessageConsumer> consumers = new ArrayList<>();

    @Override
    public void connect() throws Exception {
        ActiveMQConnectionFactory connectionFactory =
                new ActiveMQConnectionFactory(config.getBrokerUrl());
        connectionFactory.setUserName(config.getUsername());
        connectionFactory.setPassword(config.getPassword());

        connection = connectionFactory.createConnection();
        connection.setClientID("INE-DataLake-Client-01"); // IMPORTANTE: solo uno por cliente

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Suscribirse a múltiples topics de forma duradera
        int count = 1;
        for (String topicName : config.getTopicName()) {
            Topic topic = session.createTopic(topicName);
            String subscriptionName = "INE-Subscriber-" + count;
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionName);
            consumers.add(consumer);
            System.out.println("Subscribed durably to topic: " + topicName);
            count++;
        }

        connection.start();
        System.out.println("Connected to broker at: " + config.getBrokerUrl());
    }


    @Override
    public void setMessageHandler(Consumer<String> messageHandler) {
        for (MessageConsumer consumer : consumers) {
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
    }

    @Override
    public void disconnect() throws Exception {
        if (consumer != null) consumer.close();
        if (session != null) session.close();
        if (connection != null) connection.close();
    }
}