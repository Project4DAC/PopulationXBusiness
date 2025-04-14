package main.java.org.ulpgc.inefeeder.servicios.general.helpers;


import main.java.org.ulpgc.inefeeder.servicios.Output;

import java.util.HashMap;
import java.util.Map;

public class SimpleOutput implements Output {
    private final Map<String, Object> values = new HashMap<>();
    private int responseCode = 200;
    private String responseMessage = "OK";

    @Override
    public void setValue(String key, Object value) {
        values.put(key, value);
    }

    @Override
    public <T> T getValue(String key) {
        @SuppressWarnings("unchecked")
        T value = (T) values.get(key);
        return value;
    }


    @Override
    public String result() {

        Object result = values.get("jsonResponse");
        if (result == null) {
            result = values.get("url");
            if (result == null) {
                result = values.get("queryString");
            }
        }
        return result != null ? result.toString() : null;
    }

    @Override
    public void setResponse(int code, String message) {
        this.responseCode = code;
        this.responseMessage = message;
    }

    @Override
    public Map<String, Object> getResponse() {
        return Map.of();
    }


    @Override
    public String errorCode() {
        return String.format("Error %d: %s", responseCode, responseMessage);
    }

}
