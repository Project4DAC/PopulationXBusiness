package org.ulpgc.StoreBuilder.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatisticalDataTransformer implements DataTransformer {
    
    @Override
    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            
            // Add processing metadata
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            // Extract and analyze statistical data
            if (json.has("data")) {
                JSONArray statisticalData = json.getJSONArray("data");
                json.put("data_points", statisticalData.length());
                
                // Calculate basic statistics if numerical data is present
                JSONObject stats = new JSONObject();
                double sum = 0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                int numericCount = 0;
                
                for (int i = 0; i < statisticalData.length(); i++) {
                    JSONObject dataPoint = statisticalData.getJSONObject(i);
                    if (dataPoint.has("value")) {
                        try {
                            double value = dataPoint.getDouble("value");
                            sum += value;
                            min = Math.min(min, value);
                            max = Math.max(max, value);
                            numericCount++;
                        } catch (Exception e) {
                            // Skip non-numeric values
                        }
                    }
                }
                
                if (numericCount > 0) {
                    stats.put("mean", sum / numericCount);
                    stats.put("min", min);
                    stats.put("max", max);
                    stats.put("count", numericCount);
                }
                
                json.put("statistics", stats);
            }
            
            return json.toString(2); // Pretty print with 2-space indentation
        } catch (Exception e) {
            System.err.println("Error transforming statistical data: " + e.getMessage());
            return data; // Return original if processing fails
        }
    }
}