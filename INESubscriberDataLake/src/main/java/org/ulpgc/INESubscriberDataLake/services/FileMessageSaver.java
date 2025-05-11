package org.ulpgc.INESubscriberDataLake.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.ulpgc.INESubscriberDataLake.Interfaces.MessageSaver;

public class FileMessageSaver implements MessageSaver {
    private final String basePath;
    private final HttpClient httpClient;

    public FileMessageSaver(String basePath) {
        this.basePath = basePath;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void saveMessage(String message) {
        try {
            // Parse the incoming message
            JSONObject jsonMessage = new JSONObject(message);
            String url = jsonMessage.optString("url", "");
            String date = jsonMessage.optString("date", "");
            String datasetId = jsonMessage.optString("datasetId", "");

            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            // Save the original notification message
            String notificationFilename = "ine_notification_" + timestamp + ".json";
            saveToFile(message, notificationFilename);
            System.out.println("INE Notification saved to DataLake: " + basePath + File.separator + notificationFilename);

            // If URL is present, fetch and save its content
            if (url != null && !url.isEmpty()) {
                try {
                    String content = fetchUrlContent(url);
                    if (content != null) {
                        // Create a better filename using datasetId and date if available
                        String contentFilename = "ine_data_" + 
                                (!datasetId.isEmpty() ? datasetId + "_" : "") + 
                                (!date.isEmpty() ? date.replaceAll("-", "") : timestamp) + ".json";
                        saveToFile(content, contentFilename);
                        System.out.println("INE data saved to DataLake: " + basePath + File.separator + contentFilename);
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching URL content: " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.err.println("Error processing or saving message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveToFile(String content, String filename) throws IOException {
        File file = new File(basePath + File.separator + filename);

        // Ensure directory exists
        file.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }

    private String fetchUrlContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            System.err.println("Error fetching URL. Status code: " + response.statusCode());
            return null;
        }
    }
}