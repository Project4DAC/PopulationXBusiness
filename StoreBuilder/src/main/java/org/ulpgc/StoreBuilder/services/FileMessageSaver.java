//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.MessageSaver;

public class FileMessageSaver implements MessageSaver {
    private final String basePath;
    private final HttpClient httpClient;

    public FileMessageSaver(String basePath) {
        this.basePath = basePath;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
    }

    public void saveMessage(String message) {
        try {
            JSONObject jsonMessage = new JSONObject(message);
            String url = jsonMessage.optString("url", "");
            String date = jsonMessage.optString("date", "");
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String notificationFilename = "notification_" + timestamp + ".json";
            this.saveToFile(message, notificationFilename);
            System.out.println("Notification saved to DataLake: " + this.basePath + File.separator + notificationFilename);
            if (url != null && !url.isEmpty()) {
                try {
                    String content = this.fetchUrlContent(url);
                    if (content != null) {
                        String var10000 = !date.isEmpty() ? date.replaceAll("-", "") : timestamp;
                        String contentFilename = "borme_content_" + var10000 + ".json";
                        this.saveToFile(content, contentFilename);
                        System.out.println("URL content saved to DataLake: " + this.basePath + File.separator + contentFilename);
                    }
                } catch (Exception var10) {
                    Exception e = var10;
                    System.err.println("Error fetching URL content: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Exception var11) {
            Exception e = var11;
            System.err.println("Error processing or saving message: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void saveToFile(String content, String filename) throws IOException {
        File file = new File(this.basePath + File.separator + filename);
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file);

        try {
            writer.write(content);
        } catch (Throwable var8) {
            try {
                writer.close();
            } catch (Throwable var7) {
                var8.addSuppressed(var7);
            }

            throw var8;
        }

        writer.close();
    }

    private String fetchUrlContent(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("Accept", "application/json").GET().build();
        HttpResponse<String> response = this.httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return (String)response.body();
        } else {
            System.err.println("Error fetching URL. Status code: " + response.statusCode());
            return null;
        }
    }
}
