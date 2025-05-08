package org.ulpgc.BormeSubscriberDataLake.Commands;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.Command;

public class HelpCommand implements Command {
        @Override
        public void execute() {
            System.out.println("\nAvailable commands:");
            System.out.println("  help   - Show this help message");
            System.out.println("  status - Check the status of the message processor");
            System.out.println("  files  - List files in the DataLake");
            System.out.println("  stop   - Stop message processing but keep the CLI running");
            System.out.println("  exit   - Stop processing and exit the application");
        }

        @Override
        public String getDescription() {
            return "Shows available commands";
        }
    }