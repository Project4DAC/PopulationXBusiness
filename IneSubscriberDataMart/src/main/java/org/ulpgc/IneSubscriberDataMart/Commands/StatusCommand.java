package org.ulpgc.IneSubscriberDataMart.Commands;

import org.ulpgc.IneSubscriberDataMart.Interfaces.Command;
import org.ulpgc.IneSubscriberDataMart.services.MessageProcessor;

public class StatusCommand implements Command {
    private final MessageProcessor processor;

    public StatusCommand(MessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void execute() {
        System.out.println("INE message processor status: " +
                (processor.isRunning() ? "RUNNING" : "STOPPED"));
    }

    @Override
    public String getDescription() {
        return "Shows processor status";
    }
}