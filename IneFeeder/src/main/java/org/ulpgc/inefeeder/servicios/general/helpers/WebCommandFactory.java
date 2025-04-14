package org.ulpgc.inefeeder.servicios.general.helpers;

import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;
import main.java.org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;
import org.ulpgc.inefeeder.servicios.general.commands.RenderHomeCommand;

public class WebCommandFactory {
    public static Command createRenderHomeCommand(Input input, Output output) {
        return new RenderHomeCommand(input, output);
    }

    public static Command createRenderResultCommand(Input input, Output output) {
        return new RenderResultCommand(input, output);
    }
}