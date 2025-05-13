package org.ulpgc.IneSubscriberDataMart.Commands;

import org.ulpgc.INESubscriberDataLake.Interfaces.Command;
import org.ulpgc.INESubscriberDataLake.Interfaces.DataProcessor;

public class ProcessCommand implements Command {
    private final DataProcessor dataProcessor;
    
    public ProcessCommand(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
    
    @Override
    public void execute() {
        System.out.println("Processing INE data files...");
        dataProcessor.process();
        System.out.println("Processing complete.");
    }

    @Override
    public String getDescription() {
        return "Process raw INE files and move to processed and consumption zones";
    }
}