package org.ulpgc.BormeFeeder.commands.update;

import org.ulpgc.BormeFeeder.commands.query.FetchDataCommand;
import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

import javax.sql.DataSource;
import java.sql.Connection;

public class BormeFetchAndSaveDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final DataSource dataSource;

    public BormeFetchAndSaveDataCommand(Input input, Output output, DataSource dataSource) {
        this.input = input;
        this.output = output;
        this.dataSource = dataSource;
    }

    @Override
    public String execute() {
        try {

            new FetchDataCommand(input, output).execute();

            String jsonResponse = output.getValue("jsonResponse");

            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                try (Connection dbConnection = dataSource.getConnection()) {
                    input.setValue("connection", dbConnection);
                    input.setValue("jsonResponse", jsonResponse);

                    Command parseCommand = new ParseAndSaveJsonCommand(input, output);
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