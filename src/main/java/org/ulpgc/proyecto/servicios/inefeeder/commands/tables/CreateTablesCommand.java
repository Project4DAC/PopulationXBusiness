package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final Connection connection;

    public CreateTablesCommand(Input input, Output output, Connection connection) {
        this.input = input;
        this.output = output;
        this.connection = connection;
    }

    @Override
    public String execute() {
        try {
            input();
            process();
            output();
            return output.result();
        } catch (Exception e) {
            throw new RuntimeException("Error executing CreateTables command", e);
        }
    }

    private void input() {
    }

    private void process() throws SQLException {
        createTablasTable();
        createRelationTable();
        createIndex();
    }

    private void createTablasTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
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
        }
    }

    private void createRelationTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS tabla_operaciones (" +
                    "tabla_id INTEGER, " +
                    "operacion_id INTEGER, " +
                    "PRIMARY KEY(tabla_id, operacion_id), " +
                    "FOREIGN KEY(tabla_id) REFERENCES tablas(id), " +
                    "FOREIGN KEY(operacion_id) REFERENCES operaciones(id))");
        }
    }

    private void createIndex() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_tabla_operaciones " +
                    "ON tabla_operaciones(tabla_id, operacion_id)");
        }
    }

    private void output() {
        output.setValue("tablesCreated", true);
    }
}