package org.ulpgc.business.query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.business.interfaces.Command;
import org.ulpgc.business.interfaces.MessageBrokerConnector;

import java.sql.Connection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BormeTableCommandFactory {
    private static final Logger LOGGER = Logger.getLogger(BormeTableCommandFactory.class.getName());
    private static ActiveMQConnector activeMQConnector;

    public static void setActiveMQConnector(ActiveMQConnector connector) {
        activeMQConnector = connector;
    }

    public static Command createInitializeDatabaseCommand(Connection connection) {
        return () -> {
            LOGGER.info("Initializing BORME data from ActiveMQ...");

            // Si no tenemos un conector configurado, no podemos continuar
            if (activeMQConnector == null) {
                LOGGER.severe("ActiveMQ connector not configured for BORME data.");
                return "Error: ActiveMQ connector not configured";
            }

            try {
                // Crear un CompletableFuture para esperar el mensaje de ActiveMQ
                CompletableFuture<String> messageFuture = new CompletableFuture<>();

                // Configurar el handler para procesar el mensaje cuando llegue
                activeMQConnector.setMessageHandler(message -> {
                    messageFuture.complete(message);
                });

                // Esperar hasta 10 segundos para recibir un mensaje
                String bormeMessage;
                try {
                    bormeMessage = messageFuture.get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    LOGGER.warning("Timeout waiting for BORME message from ActiveMQ");
                    return "Timeout waiting for BORME data";
                }

                if (bormeMessage == null || bormeMessage.isEmpty()) {
                    LOGGER.info("No BORME data available from ActiveMQ.");
                    return "No data available";
                }

                // Procesar el mensaje JSON
                JSONArray bormeJson;
                if (bormeMessage.trim().startsWith("[")) {
                    bormeJson = new JSONArray(bormeMessage);
                } else {
                    bormeJson = new JSONArray("[" + bormeMessage + "]");
                }

                if (bormeJson.isEmpty()) {
                    LOGGER.info("Empty BORME data received from ActiveMQ.");
                    return "Empty data received";
                }

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
                            // Insertar en la base de datos si es necesario
                        }
                    }
                }
                return fechaPublicacion;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing BORME data", e);
                return "Error: " + e.getMessage();
            }
        };
    }
}