package org.ulpgc.inefeeder.servicios.general.helpers;

import org.ulpgc.inefeeder.servicios.general.Interfaces.Command;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;
import org.ulpgc.inefeeder.servicios.general.commands.RenderHomeCommand;
import org.ulpgc.inefeeder.servicios.general.commands.RenderResultCommand;

public class WebCommandFactory {
    public static Command createRenderHomeCommand(Input input, Output output) {
        return new RenderHomeCommand(input, output);
    }

    public static Command createRenderResultCommand(Input input, Output output) {
        return new RenderResultCommand(input, output);
    }
}