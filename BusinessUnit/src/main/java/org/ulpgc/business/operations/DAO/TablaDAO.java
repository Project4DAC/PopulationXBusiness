package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.ulpgc.business.operations.POJO.DatosTabla;
import org.ulpgc.business.operations.POJO.MetadatosTabla;
import org.ulpgc.business.operations.POJO.TablasOperacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TablaDAO {
    private final Gson gson;
    private final VariableDAO variableDAO;

    public TablaDAO(Gson gson, VariableDAO variableDAO) {
        this.gson = gson;
        this.variableDAO = variableDAO;
    }

    public void saveTablasOperacion(Connection conn, JsonObject jsonObject, int operacionId) throws SQLException {
        try (PreparedStatement tablasStmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO tablas (id, nombre, codigo, periodicidad, publicacion, " +
                        "periodo_ini, anyo_periodo_ini, fecha_ref_fin, ultima_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
             PreparedStatement relStmt = conn.prepareStatement(
                     "INSERT OR REPLACE INTO tabla_operaciones (tabla_id, operacion_id) VALUES (?, ?)")) {

            // Create a new array with the single object if needed
            JsonArray tablasArray;

            // Check if the object itself is a table (has Id field)
            if (jsonObject.has("Id")) {
                tablasArray = new JsonArray();
                tablasArray.add(jsonObject);
            } else {
                // Try to find an array in the JSON object
                tablasArray = null;
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    if (entry.getValue().isJsonArray()) {
                        tablasArray = entry.getValue().getAsJsonArray();
                        break;
                    }
                }

                if (tablasArray == null) {
                    throw new SQLException("Response does not contain table data: " + jsonObject);
                }
            }

            if (tablasArray.size() == 0) {
                System.out.println("No tables found in the response");
                return;
            }

            // Process each JSON object as a table individually
            for (JsonElement tableElement : tablasArray) {
                JsonObject tableObject = tableElement.getAsJsonObject();

                // Extract values directly with proper field checks
                int tablaId = tableObject.has("Id") ? tableObject.get("Id").getAsInt() : -1;
                String nombre = tableObject.has("Nombre") ? tableObject.get("Nombre").getAsString() : null;
                String codigo = tableObject.has("Codigo") ? tableObject.get("Codigo").getAsString() : null;
                int periodicidad = tableObject.has("FK_Periodicidad") ? tableObject.get("FK_Periodicidad").getAsInt() : 0;
                int publicacion = tableObject.has("FK_Publicacion") ? tableObject.get("FK_Publicacion").getAsInt() : 0;
                int periodoIni = tableObject.has("FK_Periodo_ini") ? tableObject.get("FK_Periodo_ini").getAsInt() : 0;
                String anyoPeriodoIni = tableObject.has("Anyo_Periodo_ini") ? tableObject.get("Anyo_Periodo_ini").getAsString() : null;
                String fechaRefFin = tableObject.has("FechaRef_fin") ? tableObject.get("FechaRef_fin").getAsString() : null;
                long ultimaModificacion = tableObject.has("Ultima_Modificacion") ?
                        tableObject.get("Ultima_Modificacion").getAsLong() : System.currentTimeMillis();

                // Insert into table
                tablasStmt.setInt(1, tablaId);
                tablasStmt.setString(2, nombre);
                tablasStmt.setString(3, codigo);
                tablasStmt.setInt(4, periodicidad);
                tablasStmt.setInt(5, publicacion);
                tablasStmt.setInt(6, periodoIni);
                tablasStmt.setString(7, anyoPeriodoIni);
                tablasStmt.setString(8, fechaRefFin);
                tablasStmt.setLong(9, ultimaModificacion);
                tablasStmt.executeUpdate();

                // Insert relationship
                if (tablaId > 0) {
                    relStmt.setInt(1, tablaId);
                    relStmt.setInt(2, operacionId);
                    relStmt.executeUpdate();
                    System.out.println("Added relationship between tabla " + tablaId + " and operacion " + operacionId);
                } else {
                    System.err.println("Invalid tabla ID: " + tablaId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing tablas_operacion: " + e.getMessage());
            throw new SQLException("Failed to process tablas_operacion: " + e.getMessage(), e);
        }
    }

    public void saveDatosTabla(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            // Create DatosTabla manually with type checking
            DatosTabla datosTabla = new DatosTabla();

            // Handle metadata
            if (jsonResponse.has("metadata") && !jsonResponse.get("metadata").isJsonNull()) {
                datosTabla.setMetadata(gson.fromJson(jsonResponse.get("metadata"), DatosTabla.Metadata.class));
            }

            // Handle data
            if (jsonResponse.has("data")) {
                JsonElement dataElement = jsonResponse.get("data");
                datosTabla.setData(gson.fromJson(dataElement, JsonArray.class));
            }

            // Handle variables
            if (jsonResponse.has("variables")) {
                JsonElement varsElement = jsonResponse.get("variables");
                datosTabla.setVariables(gson.fromJson(varsElement, JsonArray.class));
            }

            if (datosTabla.getMetadata() != null && datosTabla.getData() != null) {
                DatosTabla.Metadata metadata = datosTabla.getMetadata();
                JsonArray data = datosTabla.getData();
                JsonArray variables = datosTabla.getVariables();

                int tablaId = metadata.getId();

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO tablas (id, nombre, codigo, periodicidad, publicacion, " +
                                "periodo_ini, anyo_periodo_ini, fecha_ref_fin, ultima_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    stmt.setInt(1, tablaId);
                    stmt.setString(2, metadata.getNombre());
                    stmt.setString(3, metadata.getCodigo());
                    stmt.setInt(4, metadata.getPeriodicidad());
                    stmt.setInt(5, metadata.getPublicacion());
                    stmt.setInt(6, metadata.getPeriodoIni());
                    stmt.setString(7, metadata.getAnyoPeriodoIni());
                    stmt.setString(8, metadata.getFechaRefFin());
                    stmt.setLong(9, System.currentTimeMillis());
                    stmt.executeUpdate();
                }

                variableDAO.saveVariables(conn, variables, tablaId);
                saveDatos(conn, data, tablaId);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing datos_tabla JSON: " + e.getMessage());
            throw new SQLException("Failed to parse datos_tabla data", e);
        }
    }

    private void saveDatos(Connection conn, JsonArray data, int tablaId) throws SQLException {
        if (data == null || data.size() == 0) {
            return;
        }

        // First, delete existing data for this table
        try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM datos WHERE tabla_id = ?")) {
            deleteStmt.setInt(1, tablaId);
            deleteStmt.executeUpdate();
        }

        // Prepare statement to insert data
        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO datos (tabla_id, dato_json, fecha_actualizacion) VALUES (?, ?, ?)")) {

            for (JsonElement datoElement : data) {
                insertStmt.setInt(1, tablaId);
                insertStmt.setString(2, datoElement.toString());
                insertStmt.setLong(3, System.currentTimeMillis());
                insertStmt.executeUpdate();
            }
        }
    }

    public void saveMetadatosTabla(Connection conn, JsonObject jsonResponse, int tablaId) throws SQLException {
        // Convert JSON to a Map for easier handling
        MetadatosTabla metadatosTabla = new MetadatosTabla();
        metadatosTabla.setTablaId(tablaId);

        Map<String, String> metadata = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonResponse.entrySet()) {
            String key = entry.getKey();

            // Ignore keys that are already in the tablas table
            if (!key.equals("id") && !key.equals("nombre") && !key.equals("codigo") &&
                    !key.equals("periodicidad") && !key.equals("publicacion") &&
                    !key.equals("periodo_ini") && !key.equals("anyo_periodo_ini") &&
                    !key.equals("fecha_ref_fin")) {

                String value = entry.getValue().isJsonNull() ? null : entry.getValue().toString();
                metadata.put(key, value);
            }
        }
        metadatosTabla.setMetadata(metadata);

        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO metadatos_tabla (tabla_id, nombre, valor) VALUES (?, ?, ?)")) {

            for (Map.Entry<String, String> entry : metadatosTabla.getMetadata().entrySet()) {
                stmt.setInt(1, tablaId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.executeUpdate();
            }
        }
    }

    public void insertTabla(TablasOperacion tabla, int operacionId,
                          PreparedStatement tablasStmt, PreparedStatement relStmt) throws SQLException {
        int tablaId = tabla.getId();
        tablasStmt.setInt(1, tablaId);
        tablasStmt.setString(2, tabla.getNombre());
        tablasStmt.setString(3, tabla.getCodigo());
        tablasStmt.setInt(4, tabla.getPeriodicidad());
        tablasStmt.setInt(5, tabla.getPublicacion());
        tablasStmt.setInt(6, tabla.getPeriodoIni());
        tablasStmt.setString(7, tabla.getAnyoPeriodoIni());
        tablasStmt.setString(8, tabla.getFechaRefFin());
        tablasStmt.setLong(9, System.currentTimeMillis());
        tablasStmt.executeUpdate();

        relStmt.setInt(1, tablaId);
        relStmt.setInt(2, operacionId);
        relStmt.executeUpdate();
    }

    public int fetchAndSaveByOperacionCodigo(Connection conn, String codigoOperacion) {
        return 0;
    }
}