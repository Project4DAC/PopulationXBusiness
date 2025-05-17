//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;

public class NotificationTransformer implements DataTransformer {
    public NotificationTransformer() {
    }

    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            json.put("status", "processed");
            String datasetId;
            if (json.has("date")) {
                datasetId = json.getString("date");
                json.put("notification_year", datasetId.substring(0, 4));
                json.put("notification_month", datasetId.substring(5, 7));
                json.put("notification_day", datasetId.substring(8, 10));
            }

            if (json.has("datasetId")) {
                datasetId = json.getString("datasetId");
                json.put("dataset_type", this.determineDatasetType(datasetId));
            }

            boolean hasUrl = json.has("url") && !json.getString("url").isEmpty();
            boolean hasDate = json.has("date") && !json.getString("date").isEmpty();
            boolean hasDatasetId = json.has("datasetId") && !json.getString("datasetId").isEmpty();
            json.put("data_quality", hasUrl && hasDate && hasDatasetId ? "complete" : "incomplete");
            return json.toString(2);
        } catch (Exception var6) {
            Exception e = var6;
            System.err.println("Error transforming INE notification: " + e.getMessage());
            return data;
        }
    }

    private String determineDatasetType(String datasetId) {
        if (datasetId.startsWith("EPA")) {
            return "Encuesta de Población Activa";
        } else if (datasetId.startsWith("IPC")) {
            return "Índice de Precios de Consumo";
        } else if (datasetId.startsWith("PIB")) {
            return "Producto Interior Bruto";
        } else {
            return datasetId.startsWith("DEMO") ? "Datos Demográficos" : "Otros datos estadísticos";
        }
    }
}
