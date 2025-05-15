package org.ulpgc.business.service;

import java.util.List;

public class Config {
    private final String username;
    private final String password;
    private final String brokerUrl;
    private final List<String> topicNames;

    public Config(String username, String password, String brokerUrl, List<String> topicNames) {
        this.username = username;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.topicNames = topicNames;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public List<String> getTopicName() {
        return topicNames;
    }
}