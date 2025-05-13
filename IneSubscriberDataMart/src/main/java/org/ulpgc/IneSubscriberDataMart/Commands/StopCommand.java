package org.ulpgc.IneSubscriberDataMart.Commands;

import org.ulpgc.INESubscriberDataLake.Interfaces.Command;
import org.ulpgc.INESubscriberDataLake.services.MessageProcessor;

public class StopCommand implements Command {
    private final MessageProcessor processor;
    
    public StopCommand(MessageProcessor processor) {
        this.processor = processor;
    }
    
    @Override
    public void execute() {
        if (processor.isRunning()) {
            processor.stopProcessing();
            System.out.println("INE message processor stopped");
        } else {
            System.out.println("INE message processor is already stopped");
        }
    }

    @Override
    public String getDescription() {
        return "Stop message processing but keep the CLI running";
    }
}