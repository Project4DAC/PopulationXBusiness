package org.ulpgc.BormeFeeder.services.general.helpers;
import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;
import org.ulpgc.BormeFeeder.services.general.commands.RenderHomeCommand;
import org.ulpgc.BormeFeeder.services.general.commands.RenderResultCommand;

public class WebCommandFactory {
    public static Command createRenderHomeCommand(Input input, Output output) {
        return new RenderHomeCommand(input, output);
    }

    public static Command createRenderResultCommand(Input input, Output output) {
        return new RenderResultCommand(input, output);
    }
}