//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;
import org.ulpgc.StoreBuilder.services.MessageProcessor;

public class StatusCommand implements Command {
    private final MessageProcessor processor;

    public StatusCommand(MessageProcessor processor) {
        this.processor = processor;
    }

    public void execute() {
        System.out.println("Message processor status: " + (this.processor.isRunning() ? "RUNNING" : "STOPPED"));
    }

    public String getDescription() {
        return "Shows processor status";
    }
}
