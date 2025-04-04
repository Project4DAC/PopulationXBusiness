package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateOperationsTableCommand implements Command {
    private final Input input;
    private final Output output;

    public CreateOperationsTableCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        Connection conn = input.getValue("connection");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS operaciones (" +
                    "id INT PRIMARY KEY, " +
                    "COD_IOE TEXT," +
                    "nombre TEXT, " +
                    "codigo TEXT," +
                    "url_operacion TEXT)");

            output.setValue("tableCreated", true);
            return "Operations table created successfully";
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create operations table", e);
        }
    }
}