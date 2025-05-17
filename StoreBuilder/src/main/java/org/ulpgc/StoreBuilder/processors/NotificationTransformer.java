//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.processors;

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
            if (json.has("date")) {
                String date = json.getString("date");
                json.put("notification_year", date.substring(0, 4));
                json.put("notification_month", date.substring(5, 7));
                json.put("notification_day", date.substring(8, 10));
            }

            boolean hasUrl = json.has("url") && !json.getString("url").isEmpty();
            boolean hasDate = json.has("date") && !json.getString("date").isEmpty();
            json.put("data_quality", hasUrl && hasDate ? "complete" : "incomplete");
            return json.toString(2);
        } catch (Exception var5) {
            Exception e = var5;
            System.err.println("Error transforming notification: " + e.getMessage());
            return data;
        }
    }
}
