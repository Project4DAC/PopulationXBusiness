package org.ulpgc.BormeFeeder.commands.update;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ulpgc.BormeFeeder.commands.query.BuildURLCommand;
import org.ulpgc.BormeFeeder.services.Command;
import org.ulpgc.BormeFeeder.services.Input;
import org.ulpgc.BormeFeeder.services.Output;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

public class SaveDataCommand implements Command {
    private final Input input;
    private final Output output;
    private final JsonObject jsonResponse;

    public SaveDataCommand(Input input, Output output, JsonObject jsonResponse) {
        this.input = input;
        this.output = output;
        this.jsonResponse = jsonResponse;
    }

    @Override
    public String execute() {
        try (Connection conn = input.getValue("connection")) {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                savePublicacion(conn, jsonResponse);
                JsonObject data = jsonResponse.getAsJsonObject("data");
                JsonObject sumario = data.getAsJsonObject("sumario");
                JsonArray diarioArray = sumario.getAsJsonArray("diario");

                if (diarioArray != null) {
                    for (JsonElement diarioElem : diarioArray) {
                        JsonObject diario = diarioElem.getAsJsonObject();
                        int publicacionId = getLastInsertId(conn, "publicaciones");
                        int diarioId = saveDiario(conn, diario, publicacionId);

                        JsonArray secciones = diario.getAsJsonArray("seccion");
                        if (secciones != null) {
                            for (JsonElement seccionElem : secciones) {
                                JsonObject seccion = seccionElem.getAsJsonObject();
                                int seccionId = saveSeccion(conn, seccion, diarioId);

                                JsonArray items = seccion.getAsJsonArray("item");
                                if (items != null) {
                                    for (JsonElement itemElem : items) {
                                        JsonObject item = itemElem.getAsJsonObject();
                                        saveItem(conn, item, seccionId);
                                        // Also save to the 'registros' table
                                        saveRegistro(conn, sumario.getAsJsonObject("metadatos"), seccion, item);
                                    }
                                }
                            }
                        }
                    }
                }
                conn.commit();
                return "success";
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                output.setValue("error", "Error occurred while saving BORME data: " + e.getMessage());
                return "error";
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            output.setValue("error", "Error connecting to the database: " + e.getMessage());
            return "error";
        }
    }

    private void savePublicacion(Connection conn, JsonObject jsonResponse) throws SQLException {
        JsonObject data = jsonResponse.getAsJsonObject("data");
        JsonObject sumario = data.getAsJsonObject("sumario");
        JsonObject metadatos = sumario.getAsJsonObject("metadatos");
        JsonObject status = jsonResponse.getAsJsonObject("status");

        String nombre = getStringOrNull(metadatos, "publicacion");
        String fechaPublicacion = getStringOrNull(metadatos, "fecha_publicacion");
        int codigoRespuesta = getIntOrNull(status, "code", -1);
        String textoRespuesta = getStringOrNull(status, "text");

        String sql = "INSERT OR IGNORE INTO publicaciones (nombre, fecha_publicacion, codigo_respuesta, texto_respuesta) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, fechaPublicacion);
            stmt.setInt(3, codigoRespuesta);
            stmt.setString(4, textoRespuesta);
            stmt.executeUpdate();
        }
    }

    private int saveDiario(Connection conn, JsonObject diario, int publicacionId) throws SQLException {
        JsonObject sumarioDiario = diario.getAsJsonObject("sumario_diario");
        JsonObject urlPdf = sumarioDiario.getAsJsonObject("url_pdf");

        int numero = getIntOrNull(diario, "numero", -1);
        String identificador = getStringOrNull(sumarioDiario, "identificador");
        String url = getStringOrNull(urlPdf, "texto");
        int tamanyoBytes = getIntOrNull(urlPdf, "szBytes", -1);
        int tamanyoKBytes = getIntOrNull(urlPdf, "szKBytes", -1);

        String sql = "INSERT OR IGNORE INTO diarios (numero, identificador, url_pdf, tamanyo_bytes, tamanyo_kbytes, publicacion_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setString(2, identificador);
            stmt.setString(3, url);
            stmt.setInt(4, tamanyoBytes);
            stmt.setInt(5, tamanyoKBytes);
            stmt.setInt(6, publicacionId);
            stmt.executeUpdate();
            return getLastInsertId(conn, "diarios");
        }
    }

    private int saveSeccion(Connection conn, JsonObject seccion, int diarioId) throws SQLException {
        String codigo = getStringOrNull(seccion, "codigo");
        String nombre = getStringOrNull(seccion, "nombre");

        String sql = "INSERT OR IGNORE INTO secciones (codigo, nombre, diario_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            stmt.setString(2, nombre);
            stmt.setInt(3, diarioId);
            stmt.executeUpdate();
            return getLastInsertId(conn, "secciones");
        }
    }

    private void saveItem(Connection conn, JsonObject item, int seccionId) throws SQLException {
        JsonObject urlPdf = item.getAsJsonObject("url_pdf");

        String identificador = getStringOrNull(item, "identificador");
        String titulo = getStringOrNull(item, "titulo");
        String url = getStringOrNull(urlPdf, "texto");
        String urlHtml = getStringOrNull(item, "url_html");
        String urlXml = getStringOrNull(item, "url_xml");
        int tamanyoBytes = getIntOrNull(urlPdf, "szBytes", -1);
        int tamanyoKBytes = getIntOrNull(urlPdf, "szKBytes", -1);
        int paginaInicial = getIntOrNull(urlPdf, "pagina_inicial", -1);
        int paginaFinal = getIntOrNull(urlPdf, "pagina_final", -1);

        String sql = "INSERT OR IGNORE INTO items (identificador, titulo, url_pdf, url_html, url_xml, tamanyo_bytes, tamanyo_kbytes, pagina_inicial, pagina_final, seccion_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, identificador);
            stmt.setString(2, titulo);
            stmt.setString(3, url);
            stmt.setString(4, urlHtml);
            stmt.setString(5, urlXml);
            stmt.setInt(6, tamanyoBytes);
            stmt.setInt(7, tamanyoKBytes);
            stmt.setInt(8, paginaInicial);
            stmt.setInt(9, paginaFinal);
            stmt.setInt(10, seccionId);
            stmt.executeUpdate();
        }
    }

    private void saveRegistro(Connection conn, JsonObject metadatos, JsonObject seccion, JsonObject item) throws SQLException {
        String fecha = getStringOrNull(metadatos, "fecha_publicacion");
        String tipo = getStringOrNull(seccion, "nombre");
        String nombreEmpresa = getStringOrNull(item, "titulo");

        String cifEmpresa = null;

        String sql = "INSERT OR REPLACE INTO registros (fecha, tipo, empresa_nombre, empresa_cif) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fecha);
            stmt.setString(2, tipo);
            stmt.setString(3, nombreEmpresa);
            stmt.setString(4, cifEmpresa);
            stmt.executeUpdate();
        }
    }

    private String getStringOrNull(JsonObject json, String key) {
        return (json != null && json.has(key) && !json.get(key).isJsonNull())
                ? json.get(key).getAsString()
                : null;
    }

    private Integer getIntOrNull(JsonObject json, String key, int defaultValue) {
        return (json != null && json.has(key) && !json.get(key).isJsonNull())
                ? json.get(key).getAsInt()
                : defaultValue;
    }

    private int getLastInsertId(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT last_insert_rowid() FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1; // Or throw an exception if no ID was found
        }
    }
}