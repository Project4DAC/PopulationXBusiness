package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneOperacionesResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class SaveOperationsCommand implements Command {
    private final Input input;
    private final Output output;
    private final Connection connection;

    private static final int BATCH_SIZE = 1000;
    private static final String INSERT_OPERACION_QUERY =
            "INSERT OR REPLACE INTO operaciones (Id, Cod_IOE, nombre, Codigo, url_operacion) VALUES (?,?,?,?,?)";

    public SaveOperationsCommand(Input input, Output output, Connection connection) {
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
            throw new RuntimeException("Error executing SaveOperations command", e);
        }
    }

    private void input() {
        IneOperacionesResponse[] operaciones = input.getValue("operaciones");
        if (operaciones == null) {
            throw new IllegalArgumentException("Las operaciones no pueden ser nulas.");
        }
    }

    private void process() throws SQLException {
        IneOperacionesResponse[] operaciones = input.getValue("operaciones");

        createOperacionesTable();
        saveOperacionesWithBatchProcessing(operaciones);
    }

    private void createOperacionesTable() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS operaciones (" +
                    "id INT PRIMARY KEY, " +
                    "COD_IOE TEXT," +
                    "nombre TEXT, " +
                    "codigo TEXT," +
                    "url_operacion TEXT)");
        }
    }

    private void saveOperacionesWithBatchProcessing(IneOperacionesResponse[] operaciones) throws SQLException {
        boolean originalAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try (PreparedStatement pstmt = connection.prepareStatement(INSERT_OPERACION_QUERY)) {
            int newOperationsCount = 0;

            for (int i = 0; i < operaciones.length; i++) {
                IneOperacionesResponse op = operaciones[i];
                prepareOperacionStatement(pstmt, op);
                pstmt.addBatch();
                newOperationsCount++;

                if ((i + 1) % BATCH_SIZE == 0 || i == operaciones.length - 1) {
                    pstmt.executeBatch();
                }
            }

            connection.commit();
            output.setValue("operationsAdded", newOperationsCount);
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    private void prepareOperacionStatement(PreparedStatement pstmt, IneOperacionesResponse op) throws SQLException {
        pstmt.setInt(1, op.getId());
        pstmt.setString(2, op.getCod_IOE() != null ? op.getCod_IOE() : "");
        pstmt.setString(3, op.getNombre() != null ? op.getNombre() : "");
        pstmt.setString(4, op.getCodigo() != null ? op.getCodigo() : "");
        pstmt.setString(5, op.getUrl() != null ? op.getUrl() : "");
    }

    private void output() {
    }
}