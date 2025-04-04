package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ObtainOperationIdsCommand implements Command {
    private final Input input;
    private final Output output;

    public ObtainOperationIdsCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        Connection conn = input.getValue("connection");

        try {
            List<Integer> operationIds = new ArrayList<>();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT id FROM operaciones")) {

                while (rs.next()) {
                    operationIds.add(rs.getInt("id"));
                }
            }

            output.setValue("operationIds", operationIds);
            return "Retrieved " + operationIds.size() + " operation IDs";
        } catch (SQLException e) {
            throw new RuntimeException("Failed to obtain operation IDs", e);
        }
    }
}