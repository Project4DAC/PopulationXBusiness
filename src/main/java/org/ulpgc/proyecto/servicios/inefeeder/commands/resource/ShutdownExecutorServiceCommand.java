package org.ulpgc.proyecto.servicios.inefeeder.commands.resource;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class ShutdownExecutorServiceCommand implements Command {
    private final Input input;
    private final Output output;

    public ShutdownExecutorServiceCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        ExecutorService executorService = input.getValue("executorService");

        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }

            output.setValue("shutdownCompleted", true);
            return "ExecutorService shutdown completed";
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            output.setValue("shutdownCompleted", false);
            return "ExecutorService shutdown interrupted";
        }
    }
}