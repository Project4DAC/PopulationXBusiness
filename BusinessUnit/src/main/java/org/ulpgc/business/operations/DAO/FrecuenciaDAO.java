package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.ulpgc.business.operations.POJO.Frecuencia;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class FrecuenciaDAO {
    private final Gson gson;

    public FrecuenciaDAO(Gson gson) {
        this.gson = gson;
    }

    public void saveFrecuencias(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            if (jsonResponse.has("frecuencias")) {
                JsonArray frecuenciasArray = gson.fromJson(jsonResponse.get("frecuencias"), JsonArray.class);
                Type listType = new TypeToken<List<Frecuencia>>() {}.getType();
                List<Frecuencia> frecuencias = gson.fromJson(frecuenciasArray, listType);

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO frecuencias (id, nombre, descripcion) VALUES (?, ?, ?)")) {

                    for (Frecuencia frecuencia : frecuencias) {
                        stmt.setInt(1, frecuencia.getId());
                        stmt.setString(2, frecuencia.getNombre());
                        stmt.setString(3, frecuencia.getDescripcion());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing frecuencias JSON: " + e.getMessage());
            throw new SQLException("Failed to parse frecuencias data", e);
        }
    }
}