package org.ulpgc.BormeSubscriberDataLake.Commands;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.Command;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataProcessor;

public class ProcessCommand implements Command {
    private final DataProcessor dataProcessor;

    public ProcessCommand(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    @Override
    public void execute() {
        System.out.println("Processing raw files and moving to processed and consumption zones...");
        dataProcessor.process();
        System.out.println("Processing complete!");
    }

    @Override
    public String getDescription() {
        return "Processes raw files and moves them to processed and consumption zones";
    }
}