package org.ulpgc.StoreBuilder.services;

import org.ulpgc.StoreBuilder.Commands.*;
import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;

import java.util.Map;
import java.util.Scanner;

public class CommandLineInterface {
    private final Map<String, MessageProcessor> processors;
    private final Map<String, DataProcessor> dataProcessors;
    private final Scanner scanner;
    private boolean running = true;

    public CommandLineInterface(Map<String, MessageProcessor> processors,
                                Map<String, DataProcessor> dataProcessors) {
        this.processors = processors;
        this.dataProcessors = dataProcessors;
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
        String[] parts = input.split("\\s+");
        if (parts.length == 0) return null;

        String commandName = parts[0].toLowerCase();

        switch (commandName) {
            case "help":
                return new HelpCommand();

            case "status":
            case "stop":
            case "exit":
            case "process":
                if (parts.length < 2) {
                    System.out.println("Usage: " + commandName + " <source>");
                    return null;
                }
                String source = parts[1].toLowerCase();
                MessageProcessor processor = processors.get(source);
                DataProcessor dataProcessor = dataProcessors.get(source);

                if (processor == null || dataProcessor == null) {
                    System.out.println("Unknown source: " + source);
                    return null;
                }

                switch (commandName) {
                    case "status":
                        return new StatusCommand(processor);
                    case "stop":
                        return new StopCommand(processor, this);
                    case "exit":
                        return new ExitCommand(processor, this);
                    case "process":
                        return new ProcessCommand(dataProcessor);
                }
                break;

            case "files":
                return new ListFilesCommand();
        }

        return null;
    }

    public void stop() {
        running = false;
    }
}
