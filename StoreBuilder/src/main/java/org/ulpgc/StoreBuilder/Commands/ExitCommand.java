//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.services.MessageProcessor;

public class ExitCommand implements Command {
    private final MessageProcessor processor;
    private final CommandLineInterface cli;

    public ExitCommand(MessageProcessor processor, CommandLineInterface cli) {
        this.processor = processor;
        this.cli = cli;
    }

    public void execute() {
        if (this.processor.isRunning()) {
            this.processor.stopProcessing();
        }

        System.out.println("Exiting application. Goodbye!");
        this.cli.stop();
    }

    public String getDescription() {
        return "Exits the application";
    }
}
