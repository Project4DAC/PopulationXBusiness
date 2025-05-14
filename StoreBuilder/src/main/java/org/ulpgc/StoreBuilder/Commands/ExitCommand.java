package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.services.CommandLineInterface;
import org.ulpgc.StoreBuilder.services.MessageProcessor;

public class ExitCommand implements Command {
        private final MessageProcessor processor;
        private final CommandLineInterface cli;

        public ExitCommand(MessageProcessor processor, CommandLineInterface cli) {
            this.processor = processor;
            this.cli = cli;
        }

        @Override
        public void execute() {
            if (processor.isRunning()) {
                processor.stopProcessing();
            }
            System.out.println("Exiting application. Goodbye!");
            cli.stop();
        }

        @Override
        public String getDescription() {
            return "Exits the application";
        }
    }
