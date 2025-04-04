package org.ulpgc.proyecto.servicios.inefeeder.commands.tables;

import org.ulpgc.proyecto.servicios.Command;
import org.ulpgc.proyecto.servicios.Input;
import org.ulpgc.proyecto.servicios.Output;
import org.ulpgc.proyecto.servicios.inefeeder.IneTablaResponse;


import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ProcessBatchTablesCommand implements Command {
    private final Input input;
    private final Output output;
    private final int batchSize;

    public ProcessBatchTablesCommand(Input input, Output output, int batchSize) {
        this.input = input;
        this.output = output;
        this.batchSize = batchSize;
    }

    @Override
    public String execute() {
        PreparedStatement pstmtTablas = input.getValue("pstmtTablas");
        PreparedStatement pstmtRelations = input.getValue("pstmtRelations");
        IneTablaResponse[] tablas = input.getValue("tablas");
        Integer operacionId = input.getValue("operacionId");

        try {
            int tablesProcessed = 0;

            for (IneTablaResponse tabla : tablas) {
                if (tabla.getId() == null) continue;

                prepareTablaStatement(pstmtTablas, tabla);
                pstmtTablas.addBatch();
                tablesProcessed++;

                pstmtRelations.setInt(1, tabla.getId());
                pstmtRelations.setInt(2, operacionId);
                pstmtRelations.addBatch();

                if (tablesProcessed % batchSize == 0) {
                    pstmtTablas.executeBatch();
                    pstmtRelations.executeBatch();
                }
            }

            pstmtTablas.executeBatch();
            pstmtRelations.executeBatch();

            output.setValue("tablesProcessed", tablesProcessed);
            return "Processed " + tablesProcessed + " tables for operation " + operacionId;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to process batch tables", e);
        }
    }

    private void prepareTablaStatement(PreparedStatement pstmt, IneTablaResponse tabla) throws SQLException {
        pstmt.setInt(1, tabla.getId());
        pstmt.setString(2, tabla.getNombre() != null ? tabla.getNombre() : "");
        pstmt.setString(3, tabla.getCodigo() != null ? tabla.getCodigo() : "");

        if (tabla.getFK_Periodicidad() != null)
            pstmt.setInt(4, tabla.getFK_Periodicidad());
        else
            pstmt.setNull(4, Types.INTEGER);

        if (tabla.getFK_Publicacion() != null)
            pstmt.setInt(5, tabla.getFK_Publicacion());
        else
            pstmt.setNull(5, Types.INTEGER);

        if (tabla.getFK_Periodo_ini() != null)
            pstmt.setInt(6, tabla.getFK_Periodo_ini());
        else
            pstmt.setNull(6, Types.INTEGER);

        pstmt.setString(7, tabla.getAnyo_Periodo_ini());
        pstmt.setString(8, tabla.getFechaRef_fin());

        if (tabla.getUltima_Modificacion() != null)
            pstmt.setLong(9, tabla.getUltima_Modificacion());
        else
            pstmt.setNull(9, Types.BIGINT);
    }
}