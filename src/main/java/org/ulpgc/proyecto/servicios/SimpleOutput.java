package org.ulpgc.proyecto.servicios;

import java.util.HashMap;
import java.util.Map;

public class SimpleOutput implements Output {
    private final Map<String, Object> values = new HashMap<>();

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
}