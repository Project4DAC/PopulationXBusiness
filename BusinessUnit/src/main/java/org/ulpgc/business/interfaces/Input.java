package org.ulpgc.business.interfaces;

// Interface Input
public interface Input {
    <T> T getValue(String key);
    void setValue(String key, Object value);
}

