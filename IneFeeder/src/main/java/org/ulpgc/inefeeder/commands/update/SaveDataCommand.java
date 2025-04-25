package org.ulpgc.inefeeder.commands.update;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.ulpgc.inefeeder.commands.query.POJO.*;
import org.ulpgc.inefeeder.servicios.Command;
import org.ulpgc.inefeeder.servicios.Input;
import org.ulpgc.inefeeder.servicios.Output;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final String functionName;
    private final JsonObject jsonResponse;
    private final Gson gson;

    public SaveDataCommand(Input input, Output output, String functionName, JsonObject jsonResponse) {
        this.input = input;
        this.output = output;
        this.functionName = functionName;
        this.jsonResponse = jsonResponse;

        // Set up Gson with custom type adapters for problematic fields
        this.gson = new GsonBuilder()
                .registerTypeAdapter(JsonArray.class, new JsonArrayAdapter())
                .create();
    }

    // Custom adapter for JsonArray - converts primitives and objects to arrays when needed
    private static class JsonArrayAdapter implements JsonDeserializer<JsonArray> {
        @Override
        public JsonArray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonArray()) {
                return json.getAsJsonArray();
            } else {
                // Convert non-array to a single-element array
                JsonArray array = new JsonArray();
                if (!json.isJsonNull()) {
                    array.add(json);
                }
                return array;
            }
        }
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
            switch (functionName.toUpperCase()) {
                case "OPERACIONES_DISPONIBLES":
                    saveOperacion(conn, jsonResponse);
                    break;
                case "TABLAS_OPERACION":
                    saveTablasOperacion(conn, jsonResponse);
                    break;
                case "DATOS_TABLA":
                    saveDatosTabla(conn, jsonResponse);
                    break;
                case "FRECUENCIAS":
                    saveFrecuencias(conn, jsonResponse);
                    break;
                case "VALORES_VARIABLE":
                    saveValoresVariable(conn, jsonResponse);
                    break;
                case "METADATOS_TABLA":
                    saveMetadatosTabla(conn, jsonResponse);
                    break;
                case "PROPIEDADES_VARIABLE":
                    savePropiedadesVariable(conn, jsonResponse);
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
        try {
            // Parse JSON into POJO, using our custom type-safe Gson instance
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

    private void saveDatosTabla(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            // Create DatosTabla manually with type checking
            DatosTabla datosTabla = new DatosTabla();

            // Handle metadata
            if (jsonResponse.has("metadata") && !jsonResponse.get("metadata").isJsonNull()) {
                datosTabla.setMetadata(gson.fromJson(jsonResponse.get("metadata"), DatosTabla.Metadata.class));
            }

            // Handle data - use our custom adapter implicitly through gson
            if (jsonResponse.has("data")) {
                JsonElement dataElement = jsonResponse.get("data");
                datosTabla.setData(gson.fromJson(dataElement, JsonArray.class));
            }

            // Handle variables - use our custom adapter implicitly through gson
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

                saveVariables(conn, variables, tablaId);
                saveDatos(conn, data, tablaId, variables);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing datos_tabla JSON: " + e.getMessage());
            throw new SQLException("Failed to parse datos_tabla data", e);
        }
    }

    private void saveDatos(Connection conn, JsonArray data, int tablaId, JsonArray variables) throws SQLException {
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

    private void saveVariables(Connection conn, JsonArray variables, int tablaId) throws SQLException {
        if (variables == null || variables.size() == 0) {
            return;
        }

        try (
                PreparedStatement varStmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO variables (id, nombre, tabla_id) VALUES (?, ?, ?)");
                PreparedStatement valStmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO variable_valores (id, nombre, variable_id) VALUES (?, ?, ?)");
                PreparedStatement propStmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO propiedades_variable (variable_id, orden, tipo, es_clave) VALUES (?, ?, ?, ?)")) {

            for (JsonElement varElem : variables) {
                JsonObject variable = varElem.getAsJsonObject();
                int variableId = variable.get("id").getAsInt();
                String nombreVar = variable.has("nombre") && !variable.get("nombre").isJsonNull() ?
                        variable.get("nombre").getAsString() : null;

                varStmt.setInt(1, variableId);
                varStmt.setString(2, nombreVar);
                varStmt.setInt(3, tablaId);
                varStmt.executeUpdate();

                if (variable.has("valores") && !variable.get("valores").isJsonNull()) {
                    // Use our custom adapter implicitly through gson
                    JsonArray valores = gson.fromJson(variable.get("valores"), JsonArray.class);

                    for (JsonElement valElem : valores) {
                        JsonObject valor = valElem.getAsJsonObject();
                        String id = valor.has("id") ? valor.get("id").getAsString() : null;
                        String nombre = valor.has("nombre") && !valor.get("nombre").isJsonNull() ?
                                valor.get("nombre").getAsString() : null;

                        valStmt.setString(1, id);
                        valStmt.setString(2, nombre);
                        valStmt.setInt(3, variableId);
                        valStmt.executeUpdate();
                    }
                }

                if (variable.has("propiedades") && !variable.get("propiedades").isJsonNull()) {
                    JsonObject props = variable.getAsJsonObject("propiedades");
                    int orden = props.has("orden") && !props.get("orden").isJsonNull() ?
                            props.get("orden").getAsInt() : 0;
                    String tipo = props.has("tipo") && !props.get("tipo").isJsonNull() ?
                            props.get("tipo").getAsString() : null;
                    boolean esClave = props.has("es_clave") && props.get("es_clave").getAsBoolean();

                    propStmt.setInt(1, variableId);
                    propStmt.setInt(2, orden);
                    propStmt.setString(3, tipo);
                    propStmt.setBoolean(4, esClave);
                    propStmt.executeUpdate();
                }
            }
        }
    }

    private void saveTablasOperacion(Connection conn, JsonObject jsonObject) throws SQLException {
        Map<String, String> params = input.getValue("params");
        String operacionInput;
        if (params == null) {
            operacionInput = input.getValue("inputValue");
        }else{
            operacionInput = params.get("idOperacion");
        }
        int operacionId = Integer.parseInt(operacionInput);

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
    /**
     * Find an operation ID by its code
     * @param conn Database connection
     * @param code Operation code to look up
     * @return Operation ID, or -1 if not found
     */
    private int findOperacionIdByCode(Connection conn, String code) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id FROM operaciones WHERE codigo = ?")) {
            stmt.setString(1, code);
            try (var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        return -1;
    }

    private void insertTabla(TablasOperacion tabla, int operacionId,
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

    private void saveFrecuencias(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            if (jsonResponse.has("frecuencias")) {
                // Use our custom adapter implicitly through gson
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

    private void saveValoresVariable(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            Map<String, String> params = input.getValue("params");
            int variableId;
            if (params == null) {
                variableId = input.getValue("inputValue");
            }else{
                variableId = Integer.parseInt(params.get("variableId"));
                }

            if (jsonResponse.has("valores")) {
                // Use custom adapter implicitly through gson
                JsonElement valoresElement = jsonResponse.get("valores");
                JsonArray valores = gson.fromJson(valoresElement, JsonArray.class);

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT OR REPLACE INTO variable_valores (id, nombre, variable_id) VALUES (?, ?, ?)")) {

                    for (JsonElement valElem : valores) {
                        JsonObject valor = valElem.getAsJsonObject();
                        String id = valor.has("id") ? valor.get("id").getAsString() : null;
                        String nombre = valor.has("nombre") && !valor.get("nombre").isJsonNull() ?
                                valor.get("nombre").getAsString() : null;

                        stmt.setString(1, id);
                        stmt.setString(2, nombre);
                        stmt.setInt(3, variableId);
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing valores_variable JSON: " + e.getMessage());
            throw new SQLException("Failed to parse valores_variable data", e);
        }
    }

    private void saveMetadatosTabla(Connection conn, JsonObject jsonResponse) throws SQLException {
        Map<String, String> params = input.getValue("params");
        int tablaId;
        if (params == null) {
            tablaId = Integer.parseInt("inputValue");
        }else{
            tablaId = Integer.parseInt(params.get("idTabla"));
        }

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

    private void savePropiedadesVariable(Connection conn, JsonObject jsonResponse) throws SQLException {
        try {
            Map<String, String> params = input.getValue("params");
            int variableId;
            if (params == null) {
                variableId = Integer.parseInt("inputValue");
            }else{
                variableId = Integer.parseInt(params.get("idVariable"));
            }

            // Convert to POJO with our custom type-safe gson
            PropiedadesVariable propiedadesVariable = gson.fromJson(jsonResponse, PropiedadesVariable.class);
            propiedadesVariable.setVariableId(variableId);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO propiedades_variable (variable_id, orden, tipo, es_clave) VALUES (?, ?, ?, ?)")) {

                stmt.setInt(1, propiedadesVariable.getVariableId());
                stmt.setInt(2, propiedadesVariable.getOrden());
                stmt.setString(3, propiedadesVariable.getTipo());
                stmt.setBoolean(4, propiedadesVariable.isEsClave());
                stmt.executeUpdate();
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Error parsing propiedades_variable JSON: " + e.getMessage());
            throw new SQLException("Failed to parse propiedades_variable data", e);
        }
    }
}