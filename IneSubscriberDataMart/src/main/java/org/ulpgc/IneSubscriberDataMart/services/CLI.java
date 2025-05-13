package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Commands.*;
import org.ulpgc.IneSubscriberDataMart.Interfaces.Command;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CLI {
    private final Map<String, Command> commands = new HashMap<>();
    private final MessageProcessor processor;
    private final DataProcessor dataProcessor;
    private boolean running = true;
    
    public CLI(MessageProcessor processor, DataProcessor dataProcessor) {
        this.processor = processor;
        this.dataProcessor = dataProcessor;
        
        // Register commands
        registerCommands();
    }
    
    private void registerCommands() {
        commands.put("help", new HelpCommand());
        commands.put("status", new StatusCommand(processor));
        commands.put("files", new FilesCommand());
        commands.put("process", new ProcessCommand(dataProcessor));
        commands.put("stop", new StopCommand(processor));
        commands.put("exit", new ExitCommand(this, processor));
    }
    
    public void start() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("INE Subscriber Data Lake CLI");
        System.out.println("Type 'help' for available commands");
        
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            // Extract command and arguments
            String[] parts = input.split("\\s+", 2);
            String commandName = parts[0].toLowerCase();
            
            Command command = commands.get(commandName);
            if (command != null) {
                command.execute();
            } else {
                System.out.println("Unknown command: " + commandName);
                System.out.println("Type 'help' for available commands");
            }
        }
        
        scanner.close();
    }
    
    public void stop() {
        running = false;
        System.out.println("Exiting CLI...");
    }
}