package org.ulpgc.BormeSubscriberDataLake.Interfaces;

public interface Command {
    void execute();
    String getDescription();
}