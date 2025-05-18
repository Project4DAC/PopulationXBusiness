//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.business.query;

import java.util.List;

public class Config {
    private final String username;
    private final String password;
    private final String brokerUrl;
    private final List<String> topicName;

    public Config(String username, String password, String brokerUrl, List<String> topicName) {
        this.username = username;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getBrokerUrl() {
        return this.brokerUrl;
    }

    public String getTopicName() {
        return String.valueOf(this.topicName);
    }
}
