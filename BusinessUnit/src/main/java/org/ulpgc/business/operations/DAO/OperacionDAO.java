package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.ulpgc.business.operations.POJO.Operacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OperacionDAO {
    private final Gson gson;

    public OperacionDAO(Gson gson) {
        this.gson = gson;
    }

    public void saveOperacion(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            Operacion operacion = gson.fromJson(jsonResponse, Operacion.class);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO operaciones (id, COD_IOE, nombre, codigo, url_operacion) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setInt(1, operacion.getId());
                stmt.setString(2, operacion.getCodIoE());
                stmt.setString(3, operacion.getNombre());
                stmt.setString(4, operacion.getCodigo());
                stmt.setString(5, operacion.getUrlOperacion());
                stmt.executeUpdate();
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing operacion JSON: " + e.getMessage());
            throw new SQLException("Failed to parse operacion data", e);
        }
    }

    /**
     * Find an operation ID by its code
     * @param conn Database connection
     * @param code Operation code to look up
     * @return Operation ID, or -1 if not found
     */
    public int findOperacionIdByCode(Connection conn, String code) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM operaciones WHERE codigo = ?")) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }
}