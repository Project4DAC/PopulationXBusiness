package org.ulpgc.inefeeder.commands.update;


import org.ulpgc.inefeeder.commands.query.FetchDataCommand;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Command;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Input;
import org.ulpgc.inefeeder.servicios.general.Interfaces.Output;

import javax.sql.DataSource;
import java.sql.Connection;

public class INEFetchAndSaveDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final DataSource dataSource;

    public INEFetchAndSaveDataCommand(Input input, Output output, DataSource dataSource) {
        this.input = input;
        this.output = output;
        this.dataSource = dataSource;
    }

    @Override
    public String execute() {
        try {
            String functionName = input.getValue("function");

            new FetchDataCommand(input, output).execute();

            String jsonResponse = output.getValue("jsonResponse");

            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                try (Connection dbConnection = dataSource.getConnection()) {
                    input.setValue("connection", dbConnection);
                    input.setValue("jsonResponse", jsonResponse);

                    Command parseCommand = new ParseAndSaveJsonCommand(input, output, functionName);
                    parseCommand.execute();
                }
            }
        } catch (Exception e) {
            output.setValue("error", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}