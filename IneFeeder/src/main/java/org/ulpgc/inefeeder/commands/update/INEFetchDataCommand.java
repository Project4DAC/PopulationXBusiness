package org.ulpgc.inefeeder.commands.update;


import org.ulpgc.inefeeder.commands.query.FetchDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Command;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;

import javax.sql.DataSource;
import java.sql.Connection;

public class INEFetchDataCommand implements Command {
    private final Input input;
    private final Output output;

    public INEFetchDataCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        try {
            new FetchDataCommand(input, output).execute();
        } catch (Exception e) {
            output.setValue("error", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
