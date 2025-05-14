package org.ulpgc.StoreBuilder.services;

import org.ulpgc.StoreBuilder.Commands.*;
import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;

import java.util.Scanner;

public class CommandLineInterface {
    private final MessageProcessor processor;
    private final DataProcessor dataProcessor;
    private final Scanner scanner;
    private boolean running = true;

    public CommandLineInterface(MessageProcessor processor, DataProcessor dataProcessor) {
        this.processor = processor;
        this.dataProcessor = dataProcessor;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n===== ActiveMQ Subscriber Command Interface =====");
        System.out.println("Type 'help' to see available commands");

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            Command command = parseCommand(input);
            if (command != null) {
                command.execute();
            } else {
                System.out.println("Unknown command. Type 'help' to see available commands.");
            }
        }

        scanner.close();
    }

    private Command parseCommand(String input) {
        if (input.equalsIgnoreCase("help")) {
            return new HelpCommand();
        } else if (input.equalsIgnoreCase("status")) {
            return new StatusCommand(processor);
        } else if (input.equalsIgnoreCase("stop")) {
            return new StopCommand(processor, this);
        } else if (input.equalsIgnoreCase("exit")) {
            return new ExitCommand(processor, this);
        } else if (input.equalsIgnoreCase("files")) {
            return new ListFilesCommand();
        } else if (input.equalsIgnoreCase("process")) {
            return new ProcessCommand(dataProcessor);
        }
        return null;
    }

    public void stop() {
        running = false;
    }
}