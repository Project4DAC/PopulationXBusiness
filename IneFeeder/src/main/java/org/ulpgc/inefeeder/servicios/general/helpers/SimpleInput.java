package org.ulpgc.inefeeder.servicios.general.helpers;



import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;

import java.util.HashMap;
import java.util.Map;

public class SimpleInput implements Input {
    private final Map<String, Object> values = new HashMap<>();

    @Override
    public <T> T getValue(String key) {
        @SuppressWarnings("unchecked")
        T value = (T) values.get(key);
        return value;
    }

    @Override
    public void setValue(String key, Object value) {
        values.put(key, value);
    }
}
