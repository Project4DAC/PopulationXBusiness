package org.ulpgc.inefeeder.commands.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import main.java.org.ulpgc.inefeeder.servicios.Command;
import main.java.org.ulpgc.inefeeder.servicios.Input;
import main.java.org.ulpgc.inefeeder.servicios.Output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class SaveDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final String functionName;
    private final JsonObject jsonResponse;

    public SaveDataCommand(Input input, Output output, String functionName, JsonObject jsonResponse) {
        this.input = input;
        this.output = output;
        this.functionName = functionName;
        this.jsonResponse = jsonResponse;
    }

    @Override
    public String execute() {
        if (jsonResponse == null) {
            output.setValue("dataSaved", false);
            return "No data to save";
        }

        Connection conn = input.getValue("connection");
        if (conn == null) {
            throw new IllegalArgumentException("Database connection not provided in input");
        }

        try {
            switch (functionName) {
                case "OPERACIONES_DISPONIBLES":
                    saveOperacion(conn, jsonResponse);
                    break;
                case "DATOS_TABLA":
                    saveDatosTabla(conn, jsonResponse);
                    break;
                case "TABLAS_OPERACION":
                    System.out.println("foo");
                    saveTablasOperacion(conn, jsonResponse);
                    break;
                default:
                    output.setValue("dataSaved", false);
                    return "No save handler for function: " + functionName;
            }

            output.setValue("dataSaved", true);
            return "Data saved successfully for function: " + functionName;
        } catch (Exception e) {
            output.setValue("dataSaved", false);
            throw new RuntimeException("Failed to save data for function: " + functionName, e);
        }
    }

    private void saveOperacion(Connection conn, JsonObject jsonResponse) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO operaciones (id, COD_IOE, nombre, codigo, url_operacion) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setInt(1, jsonResponse.get("Id").getAsInt());
            stmt.setString(2, getStringOrNull(jsonResponse, "COD_IOE"));
            stmt.setString(3, getStringOrNull(jsonResponse, "Nombre"));
            stmt.setString(4, getStringOrNull(jsonResponse, "Codigo"));
            stmt.setString(5, getStringOrNull(jsonResponse, "Url"));
            stmt.executeUpdate();
        }
        catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    private void saveDatosTabla(Connection conn, JsonObject jsonResponse) throws SQLException {
        if (jsonResponse.has("metadata") && jsonResponse.has("data")) {
            JsonObject metadata = jsonResponse.getAsJsonObject("metadata");
            JsonArray data = jsonResponse.getAsJsonArray("data");

            int tablaId = metadata.get("id").getAsInt();

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO tablas (id, nombre, codigo, periodicidad, publicacion, " +
                            "periodo_ini, anyo_periodo_ini, fecha_ref_fin, ultima_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setInt(1, tablaId);
                stmt.setString(2, getStringOrNull(metadata, "nombre"));
                stmt.setString(3, getStringOrNull(metadata, "codigo"));
                stmt.setInt(4, getIntOrNull(metadata, "periodicidad"));
                stmt.setInt(5, getIntOrNull(metadata, "publicacion"));
                stmt.setInt(6, getIntOrNull(metadata, "periodo_ini"));
                stmt.setString(7, getStringOrNull(metadata, "anyo_periodo_ini"));
                stmt.setString(8, getStringOrNull(metadata, "fecha_ref_fin"));
                stmt.setLong(9, System.currentTimeMillis());
                stmt.executeUpdate();
            }
        }
    }

    private void saveTablasOperacion(Connection conn, JsonObject jsonResponse) throws SQLException {
        Map<String, String> params = input.getValue("params");
        int operacionId = Integer.parseInt(params.get("idOperacion"));

        try (PreparedStatement tablasStmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO tablas (id, nombre, codigo, periodicidad, publicacion, " +
                        "periodo_ini, anyo_periodo_ini, fecha_ref_fin, ultima_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement relStmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO tabla_operaciones (tabla_id, operacion_id) VALUES (?, ?)")) {

            if (jsonResponse.has("Id")) {
                insertTabla(jsonResponse, operacionId, tablasStmt, relStmt);
            } else {
                JsonArray tablas = jsonResponse.getAsJsonArray("tablas");
                for (JsonElement tablaElem : tablas) {
                    JsonObject tabla = tablaElem.getAsJsonObject();
                    insertTabla(tabla, operacionId, tablasStmt, relStmt);
                }
            }
                  }
    }

    private void insertTabla(JsonObject tabla, int operacionId,
                             PreparedStatement tablasStmt, PreparedStatement relStmt) throws SQLException {
        int tablaId = tabla.get("Id").getAsInt();
        tablasStmt.setInt(1, tablaId);
        tablasStmt.setString(2, getStringOrNull(tabla, "nombre"));
        tablasStmt.setString(3, getStringOrNull(tabla, "codigo"));
        tablasStmt.setInt(4, getIntOrNull(tabla, "periodicidad"));
        tablasStmt.setInt(5, getIntOrNull(tabla, "publicacion"));
        tablasStmt.setInt(6, getIntOrNull(tabla, "periodo_ini"));
        tablasStmt.setString(7, getStringOrNull(tabla, "anyo_periodo_ini"));
        tablasStmt.setString(8, getStringOrNull(tabla, "fecha_ref_fin"));
        tablasStmt.setLong(9, System.currentTimeMillis());
        tablasStmt.executeUpdate();

        relStmt.setInt(1, tablaId);
        relStmt.setInt(2, operacionId);
        relStmt.executeUpdate();
    }
    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private int getIntOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : 0;
    }
}