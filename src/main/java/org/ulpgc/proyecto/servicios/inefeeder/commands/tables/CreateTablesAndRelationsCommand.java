package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTablesAndRelationsCommand implements Command {
    private final Input input;
    private final Output output;

    public CreateTablesAndRelationsCommand(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public String execute() {
        Connection conn = input.getValue("connection");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tablas (" +
                    "id INTEGER PRIMARY KEY, " +
                    "nombre TEXT, " +
                    "codigo TEXT, " +
                    "periodicidad INTEGER, " +
                    "publicacion INTEGER, " +
                    "periodo_ini INTEGER, " +
                    "anyo_periodo_ini TEXT, " +
                    "fecha_ref_fin TEXT, " +
                    "ultima_modificacion BIGINT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                    "tabla_id INTEGER, " +
                    "operacion_id INTEGER, " +
                    "PRIMARY KEY(tabla_id, operacion_id), " +
                    "FOREIGN KEY(tabla_id) REFERENCES tablas(id), " +
                    "FOREIGN KEY(operacion_id) REFERENCES operaciones(id))");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tabla_operaciones " +
                    "ON tabla_operaciones(tabla_id, operacion_id)");

            output.setValue("tablesCreated", true);
            return "Tables and relations created successfully";
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create tables and relations", e);
        }
    }
}