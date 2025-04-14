package org.ulpgc.inefeeder.commands.update;

import main.java.org.ulpgc.inefeeder.commands.query.FetchDataCommand;
import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;

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