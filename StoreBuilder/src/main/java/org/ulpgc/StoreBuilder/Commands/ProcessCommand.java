//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;

public class ProcessCommand implements Command {
    private final DataProcessor dataProcessor;

    public ProcessCommand(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }

    public void execute() {
        System.out.println("Processing raw files and moving to processed and consumption zones...");
        this.dataProcessor.process();
        System.out.println("Processing complete!");
    }

    public String getDescription() {
        return "Processes raw files and moves them to processed and consumption zones";
    }
}
