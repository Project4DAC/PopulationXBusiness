package org.ulpgc.BormeSubscriberDataLake.Commands;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.Command;
import org.ulpgc.BormeSubscriberDataLake.services.CommandLineInterface;
import org.ulpgc.BormeSubscriberDataLake.services.MessageProcessor;

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
