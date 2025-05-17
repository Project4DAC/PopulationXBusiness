package org.ulpgc.business.operations.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class IndicadorDAO {
    private Connection connection;

    public IndicadorDAO(Connection connection) {
        this.connection = connection;
    }

    public void insertarIndicador(Indicador indicador) throws SQLException {
        String sqlIndicador = "INSERT INTO Indicadores (COD, Nombre, FK_Unidad, FK_Escala) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sqlIndicador)) {
            stmt.setString(1, indicador.getCod());
            stmt.setString(2, indicador.getNombre());
            stmt.setInt(3, indicador.getFkUnidad());
            stmt.setInt(4, indicador.getFkEscala());
            stmt.executeUpdate();
        }

        insertarDatos(indicador.getCod(), indicador.getData());
    }

    private void insertarDatos(String codIndicador, List<Dato> datos) throws SQLException {
        String sqlDato = "INSERT INTO Datos (COD_Indicador, Fecha, FK_TipoDato, FK_Periodo, Anyo, Valor, Secreto) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sqlDato)) {
            for (Dato dato : datos) {
                stmt.setString(1, codIndicador);
                stmt.setLong(2, dato.getFecha());
                stmt.setInt(3, dato.getFkTipoDato());
                stmt.setInt(4, dato.getFkPeriodo());
                stmt.setInt(5, dato.getAnyo());
                stmt.setDouble(6, dato.getValor());
                stmt.setBoolean(7, dato.isSecreto());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}
