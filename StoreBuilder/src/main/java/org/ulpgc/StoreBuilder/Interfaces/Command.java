package org.ulpgc.StoreBuilder.Interfaces;

public interface Command {
    void execute();
    String getDescription();
}