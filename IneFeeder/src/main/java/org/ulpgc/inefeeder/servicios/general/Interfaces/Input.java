package org.ulpgc.inefeeder.servicios.general.Interfaces;

// Interface Input
public interface Input {
    <T> T getValue(String key);
    void setValue(String key, Object value);
}

