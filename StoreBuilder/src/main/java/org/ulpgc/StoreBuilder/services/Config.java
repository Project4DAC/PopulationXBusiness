package org.ulpgc.StoreBuilder.services;

public class Config {
    private final String username;
    private final String password;
    private final String brokerUrl;
    private final String topicName;

    public Config(String username, String password, String brokerUrl, String topicName) {
        this.username = username;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
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

    public String getTopicName() {
        return topicName;
    }
}