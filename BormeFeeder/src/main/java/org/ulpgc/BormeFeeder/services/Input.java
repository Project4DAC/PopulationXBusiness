package org.ulpgc.BormeFeeder.services;

// Interface Input
public interface Input {
    <T> T getValue(String key);
    void setValue(String key, Object value);
}

