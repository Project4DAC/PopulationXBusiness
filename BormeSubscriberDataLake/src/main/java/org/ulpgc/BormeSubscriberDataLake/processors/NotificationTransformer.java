package org.ulpgc.BormeSubscriberDataLake.processors;

import org.json.JSONObject;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataTransformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationTransformer implements DataTransformer {
    
    @Override
    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            
            // Add processing metadata
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            json.put("status", "processed");
            
            // Extract and enhance key information
            if (json.has("date")) {
                String date = json.getString("date");
                json.put("notification_year", date.substring(0, 4));
                json.put("notification_month", date.substring(5, 7));
                json.put("notification_day", date.substring(8, 10));
            }
            
            // Add data quality indicators
            boolean hasUrl = json.has("url") && !json.getString("url").isEmpty();
            boolean hasDate = json.has("date") && !json.getString("date").isEmpty();
            json.put("data_quality", hasUrl && hasDate ? "complete" : "incomplete");
            
            return json.toString(2); // Pretty print with 2-space indentation
        } catch (Exception e) {
            System.err.println("Error transforming notification: " + e.getMessage());
            return data; // Return original if processing fails
        }
    }
}