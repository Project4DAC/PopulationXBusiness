package org.ulpgc.proyecto.servicios;

public interface Output {
    void setValue(String key, Object value);
    <T> T getValue(String key);
    String result();
}