package org.ulpgc.business.query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.business.interfaces.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.logging.Logger;

public class BormeTableCommandFactory {
    private static final Logger LOGGER = Logger.getLogger(BormeTableCommandFactory.class.getName());

    public static Command createInitializeDatabaseCommand(Connection connection) {
        return () -> {
            LOGGER.info("Loading BORME file...");

            JSONArray bormeJson = loadBormeJson();

            JSONObject data = bormeJson.getJSONObject(0).getJSONObject("data");
            JSONObject sumario = data.getJSONObject("sumario");
            JSONObject metadatos = sumario.getJSONObject("metadatos");
            String fechaPublicacion = metadatos.getString("fecha_publicacion");
            JSONArray diario = sumario.getJSONArray("diario");

            for (int i = 0; i < diario.length(); i++) {
                JSONObject entradaDiario = diario.getJSONObject(i);
                JSONObject sumarioDiario = entradaDiario.getJSONObject("sumario_diario");
                JSONArray secciones = entradaDiario.getJSONArray("seccion");

                for (int j = 0; j < secciones.length(); j++) {
                    JSONObject seccion = secciones.getJSONObject(j);
                    JSONArray items = seccion.getJSONArray("item");

                    for (int k = 0; k < items.length(); k++) {
                        JSONObject item = items.getJSONObject(k);
                        String id = item.getString("identificador");
                        String titulo = item.getString("titulo");
                        String url = item.getJSONObject("url_pdf").getString("texto");

                        LOGGER.info(String.format("[BORME] %s | %s | %s | %s", fechaPublicacion, id, titulo, url));
                        // Aquí podrías usar la conexión para insertar en base de datos si lo deseas
                    }
                }
            }
            return fechaPublicacion;
        };
    }

    private static JSONArray loadBormeJson() {
        try {
            String path = "data/borme_data.json"; // Ruta donde almacenas el archivo
            String content = Files.readString(Paths.get(path));
            return new JSONArray("[" + content + "]"); // Empaquetado como array si es un solo objeto
        } catch (IOException e) {
            throw new RuntimeException("The BORME JSON file could not be loaded.", e);
        }
    }
}
