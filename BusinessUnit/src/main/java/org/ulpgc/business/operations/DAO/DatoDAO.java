package org.ulpgc.business.operations.DAO;

import com.google.gson.*;
import org.ulpgc.business.operations.POJO.Dato;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatoDAO {
    private final Gson gson;

    public DatoDAO(Gson gson) {
        this.gson = gson;
    }

    public List<Dato> findByIndicadorCodigo(Connection conn, String codigo) throws SQLException {
        List<Dato> datos = new ArrayList<>();
        String query = "SELECT * FROM dato_indicador WHERE codigo_indicador = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            ResultSet rs = stmt.executeQuery();
        }
        return datos;
    }

    public List<Dato> findByIndicadorAndYearRange(Connection conn, String codigo, int startYear, int endYear) throws SQLException {
        List<Dato> datos = new ArrayList<>();
        String query = "SELECT * FROM dato_indicador WHERE codigo_indicador = ? AND anio BETWEEN ? AND ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, codigo);
            stmt.setInt(2, startYear);
            stmt.setInt(3, endYear);
            ResultSet rs = stmt.executeQuery();
            return datos;
        }

    }


    public boolean fetchAndSaveByTablaId(Connection conn, String messageJson) {
        try {
            JsonObject root = JsonParser.parseString(messageJson).getAsJsonObject();
            int tablaId = root.get("tablaId").getAsInt(); // Solo si lo necesitas
            JsonArray dataArray = root.getAsJsonArray("datos");

            String sql = "INSERT INTO dato_indicador (codigo_indicador, anio, valor) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (JsonElement elem : dataArray) {
                    JsonObject obj = elem.getAsJsonObject();
                    String cod = obj.get("codigo_indicador").getAsString();
                    int anyo = obj.get("anio").getAsInt();
                    double valor = obj.get("valor").getAsDouble();

                    stmt.setString(1, cod);
                    stmt.setInt(2, anyo);
                    stmt.setDouble(3, valor);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}