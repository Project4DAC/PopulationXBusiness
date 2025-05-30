package org.ulpgc.BormeFeeder.services;

import java.util.Map;

public interface Output {
    void setValue(String key, Object value);
    <T> T getValue(String key);
    String result();
    void setResponse(int code, String message);
    Map<String, Object> getResponse();
    String errorCode();
}