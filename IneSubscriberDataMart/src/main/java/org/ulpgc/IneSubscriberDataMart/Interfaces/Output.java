package org.ulpgc.IneSubscriberDataMart.Interfaces;

import com.google.gson.JsonObject;

import java.util.Map;

public interface Output {
    void save(String functionName, String message);

    void setValue(String key, Object value);
    <T> T getValue(String key);
    String result();
    void setResponse(int code, String message);
    Map<String, Object> getResponse();
    String errorCode();

    void write(String tableName, JsonObject data);
}