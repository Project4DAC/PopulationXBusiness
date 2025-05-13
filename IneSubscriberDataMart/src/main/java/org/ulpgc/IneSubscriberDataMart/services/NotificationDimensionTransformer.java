package org.ulpgc.IneSubscriberDataMart.services;

import org.json.JSONObject;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataTransformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class NotificationDimensionTransformer implements DataTransformer {
    
    @Override
    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject dimension = new JSONObject();
            
            // Generate dimension ID
            String dimensionId = UUID.randomUUID().toString().substring(0, 8);
            dimension.put("dimension_id", dimensionId);
            
            // Add dimension type
            dimension.put("dimension_type", "dataset_metadata");
            
            // Extract core dimension attributes
            if (json.has("datasetId")) {
                dimension.put("dataset_id", json.getString("datasetId"));
                dimension.put("dataset_type", determineDatasetType(json.getString("datasetId")));
            } else {
                dimension.put("dataset_id", "unknown");
                dimension.put("dataset_type", "unknown");
            }
            
            // Build time dimension components
            if (json.has("date")) {
                String date = json.getString("date");
                dimension.put("year", date.substring(0, 4));
                dimension.put("month", date.substring(5, 7));
                dimension.put("day", date.substring(8, 10));
                dimension.put("quarter", determineQuarter(Integer.parseInt(date.substring(5, 7))));
                dimension.put("semester", determineSemester(Integer.parseInt(date.substring(5, 7))));
            } else {
                String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
                dimension.put("year", now.substring(0, 4));
                dimension.put("month", now.substring(5, 7));
                dimension.put("day", now.substring(8, 10));
                dimension.put("quarter", determineQuarter(Integer.parseInt(now.substring(5, 7))));
                dimension.put("semester", determineSemester(Integer.parseInt(now.substring(5, 7))));
            }
            
            // Add source URL if available
            if (json.has("url")) {
                dimension.put("source_url", json.getString("url"));
            }
            
            // Add data quality dimensions
            boolean hasUrl = json.has("url") && !json.getString("url").isEmpty();
            boolean hasDate = json.has("date") && !json.getString("date").isEmpty();
            boolean hasDatasetId = json.has("datasetId") && !json.getString("datasetId").isEmpty();
            
            dimension.put("data_quality", (hasUrl && hasDate && hasDatasetId) ? "complete" : "incomplete");
            dimension.put("has_url", hasUrl);
            dimension.put("has_date", hasDate);
            dimension.put("has_dataset_id", hasDatasetId);
            
            // Add processing metadata
            dimension.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            dimension.put("etl_version", "1.0");
            
            return dimension.toString(2); // Pretty print with 2-space indentation
        } catch (Exception e) {
            System.err.println("Error transforming to dimension: " + e.getMessage());
            return data; // Return original if processing fails
        }
    }
    
    private String determineDatasetType(String datasetId) {
        // Logic to categorize different INE datasets based on their ID
        if (datasetId.startsWith("EPA")) {
            return "labor_force_survey";
        } else if (datasetId.startsWith("IPC")) {
            return "consumer_price_index";
        } else if (datasetId.startsWith("PIB")) {
            return "gross_domestic_product";
        } else if (datasetId.startsWith("DEMO")) {
            return "demographic_data";
        } else {
            return "other_statistical_data";
        }
    }
    
    private int determineQuarter(int month) {
        if (month <= 3) return 1;
        if (month <= 6) return 2;
        if (month <= 9) return 3;
        return 4;
    }
    
    private int determineSemester(int month) {
        return month <= 6 ? 1 : 2;
    }
}