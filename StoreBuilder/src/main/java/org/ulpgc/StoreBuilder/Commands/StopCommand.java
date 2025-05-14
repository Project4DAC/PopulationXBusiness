package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.services.CommandLineInterface;
import org.ulpgc.StoreBuilder.services.MessageProcessor;

public class StopCommand implements Command {
        private final MessageProcessor processor;
        private final CommandLineInterface cli;

        public StopCommand(MessageProcessor processor, CommandLineInterface cli) {
            this.processor = processor;
            this.cli = cli;
        }

        @Override
        public void execute() {
            if (processor.isRunning()) {
                processor.stopProcessing();
                System.out.println("Message processor stopped. CLI still running.");
            } else {
                System.out.println("Message processor is already stopped.");
            }
        }

        @Override
        public String getDescription() {
            return "Stops message processing";
        }
    }
