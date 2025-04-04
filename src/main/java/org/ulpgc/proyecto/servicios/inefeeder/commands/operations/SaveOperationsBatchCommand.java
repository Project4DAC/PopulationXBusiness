package org.ulpgc.proyecto.servicios.inefeeder.commands.operations;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneOperacionesResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SaveOperationsBatchCommand implements Command {
    private final Input input;
    private final Output output;
    private final int batchSize;
    private final String insertOperacionQuery;

    public SaveOperationsBatchCommand(Input input, Output output, int batchSize, String insertOperacionQuery) {
        this.input = input;
        this.output = output;
        this.batchSize = batchSize;
        this.insertOperacionQuery = insertOperacionQuery;
    }

    @Override
    public String execute() {
        Connection conn = input.getValue("connection");
        IneOperacionesResponse[] operaciones = input.getValue("operaciones");

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(insertOperacionQuery)) {
                int newOperationsCount = 0;

                for (int i = 0; i < operaciones.length; i++) {
                    IneOperacionesResponse op = operaciones[i];
                    prepareOperacionStatement(pstmt, op);
                    pstmt.addBatch();
                    newOperationsCount++;

                    if ((i + 1) % batchSize == 0 || i == operaciones.length - 1) {
                        pstmt.executeBatch();
                    }
                }

                conn.commit();
                output.setValue("newOperationsCount", newOperationsCount);
                return "Successfully saved " + newOperationsCount + " operations";
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save operations batch", e);
        }
    }

    private void prepareOperacionStatement(PreparedStatement pstmt, IneOperacionesResponse op) throws SQLException {
        pstmt.setInt(1, op.getId());
        pstmt.setString(2, op.getCod_IOE() != null ? op.getCod_IOE() : "");
        pstmt.setString(3, op.getNombre() != null ? op.getNombre() : "");
        pstmt.setString(4, op.getCodigo() != null ? op.getCodigo() : "");
        pstmt.setString(5, op.getUrl() != null ? op.getUrl() : "");
    }
}