package org.ulpgc.INESubscriberDataLake.Commands;

import org.ulpgc.INESubscriberDataLake.services.CLI;
import org.ulpgc.INESubscriberDataLake.Interfaces.Command;
import org.ulpgc.INESubscriberDataLake.services.MessageProcessor;

public class ExitCommand implements Command {
    private final CLI cli;
    private final MessageProcessor processor;
    
    public ExitCommand(CLI cli, MessageProcessor processor) {
        this.cli = cli;
        this.processor = processor;
    }
    
    @Override
    public void execute() {
        if (processor.isRunning()) {
            processor.stopProcessing();
        }
        cli.stop();  // Stops the CLI main loop
    }

    @Override
    public String getDescription() {
        return "Stop processing and exit the application";
    }
}