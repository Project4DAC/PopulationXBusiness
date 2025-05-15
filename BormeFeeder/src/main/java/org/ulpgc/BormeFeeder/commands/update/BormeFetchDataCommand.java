package org.ulpgc.BormeFeeder.commands.update;

import org.ulpgc.BormeFeeder.commands.query.FetchDataCommand;
import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

public class BormeFetchDataCommand implements Command {
    private final Input input;
    private final Output output;

    public BormeFetchDataCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {

            new FetchDataCommand(input, output).execute();
            return null;
    }
}