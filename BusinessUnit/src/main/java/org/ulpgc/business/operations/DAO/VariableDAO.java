package org.ulpgc.business.operations.DAO;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.ulpgc.business.operations.POJO.PropiedadesVariable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VariableDAO {
    private final Gson gson;

    public VariableDAO(Gson gson) {
        this.gson = gson;
    }

    public void saveVariables(Connection conn, JsonArray variables, int tablaId) throws SQLException {
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

    public void saveValoresVariable(Connection conn, JsonObject jsonResponse, int variableId) throws SQLException {
        try {
            if (jsonResponse.has("valores")) {
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

    public void savePropiedadesVariable(Connection conn, JsonObject jsonResponse, int variableId) throws SQLException {
        try {
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