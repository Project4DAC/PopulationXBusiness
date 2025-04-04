package org.ulpgc.proyecto.servicios.inefeeder.commands.resource;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

public class CloseResourceCommand implements Command {
    private final Input input;
    private final Output output;

    public CloseResourceCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        AutoCloseable resource = input.getValue("resource");

        if (resource != null) {
            try {
                resource.close();
                output.setValue("resourceClosed", true);
                return "Resource closed successfully";
            } catch (Exception e) {
                output.setValue("resourceClosed", false);
                throw new RuntimeException("Failed to close resource", e);
            }
        } else {
            output.setValue("resourceClosed", false);
            return "Resource is null, nothing to close";
        }
    }
}
