package main.java.org.ulpgc.inefeeder.servicios;

// Interface Input
public interface Input {
    <T> T getValue(String key);
    void setValue(String key, Object value);
}

