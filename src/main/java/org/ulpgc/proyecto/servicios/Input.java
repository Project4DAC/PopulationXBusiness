package org.ulpgc.proyecto.servicios;

import java.util.HashMap;
import java.util.Map;

// Interface Input
public interface Input {
    <T> T getValue(String key);
    void setValue(String key, Object value);
}

