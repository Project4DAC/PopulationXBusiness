package org.ulpgc.INESubscriberDataLake.Commands;

import org.ulpgc.INESubscriberDataLake.Interfaces.Command;
import org.ulpgc.INESubscriberDataLake.services.MessageProcessor;

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