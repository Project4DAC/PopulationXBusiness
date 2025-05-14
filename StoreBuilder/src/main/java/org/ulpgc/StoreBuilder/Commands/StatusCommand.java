package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.services.MessageProcessor;
public class StatusCommand implements Command {
        private final MessageProcessor processor;

        public StatusCommand(MessageProcessor processor) {
            this.processor = processor;
        }

        @Override
        public void execute() {
            System.out.println("Message processor status: " +
                    (processor.isRunning() ? "RUNNING" : "STOPPED"));
        }

        @Override
        public String getDescription() {
            return "Shows processor status";
        }
    }