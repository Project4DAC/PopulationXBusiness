package org.ulpgc.BormeSubscriberDataLake.processors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataTransformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * Transforms processed data into consumption-ready format optimized for data analytics
 * and business intelligence applications.
 */
public class ConsumptionTransformer implements DataTransformer {
    
    @Override
    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject result = new JSONObject();
            
            // Add metadata
            result.put("consumption_generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            result.put("data_source", "BORME");
            
            // Handle notification data
            if (json.has("date") && json.has("notification_day")) {
                transformNotification(json, result);
            }
            // Handle content data
            else if (json.has("companies") || json.has("statistics")) {
                transformContent(json, result);
            }
            // Unknown format, just add basic transformation
            else {
                result.put("raw_data", json);
                result.put("transformation_status", "minimal");
            }
            
            return result.toString(2);
        } catch (Exception e) {
            System.err.println("Error in consumption transformation: " + e.getMessage());
            // Create error report as JSON
            JSONObject error = new JSONObject();
            error.put("error", "Transformation failed");
            error.put("message", e.getMessage());
            error.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            error.put("original_data_sample", data.length() > 100 ? data.substring(0, 100) + "..." : data);
            return error.toString(2);
        }
    }
    
    private void transformNotification(JSONObject json, JSONObject result) {
        // Extract key notification data
        String date = json.optString("date", "");
        String url = json.optString("url", "");
        
        // Build analytics-friendly structure
        JSONObject notification = new JSONObject();
        notification.put("publication_date", date);
        notification.put("source_url", url);
        notification.put("year", json.optString("notification_year", ""));
        notification.put("month", json.optString("notification_month", ""));
        notification.put("day", json.optString("notification_day", ""));
        notification.put("data_quality", json.optString("data_quality", "unknown"));
        
        // Add to result
        result.put("notification", notification);
        result.put("document_type", "notification");
        result.put("ready_for_analytics", true);
    }
    
    private void transformContent(JSONObject json, JSONObject result) {
        // Basic document info
        result.put("document_type", "content");
        
        // Handle statistics if available
        if (json.has("statistics")) {
            result.put("statistics", json.getJSONObject("statistics"));
        }
        
        // Process companies data if available
        if (json.has("companies")) {
            transformCompanies(json.getJSONArray("companies"), result);
        }
        
        result.put("ready_for_analytics", true);
    }
    
    private void transformCompanies(JSONArray companies, JSONObject result) {
        // Company type analytics
        Set<String> sectors = new HashSet<>();
        Set<String> operationTypes = new HashSet<>();
        JSONArray simplifiedCompanies = new JSONArray();
        
        for (int i = 0; i < companies.length(); i++) {
            JSONObject company = companies.getJSONObject(i);
            
            // Extract sector info
            String name = company.optString("name", "").toLowerCase();
            if (name.contains("tech") || name.contains("tecnolog")) {
                sectors.add("technology");
            } else if (name.contains("consult")) {
                sectors.add("consulting");
            } else if (name.contains("financ") || name.contains("inver")) {
                sectors.add("financial");
            } else if (name.contains("construc")) {
                sectors.add("construction");
            } else {
                sectors.add("other");
            }
            
            // Track operation types
            String operation = company.optString("operation", "unknown");
            operationTypes.add(operation);
            
            // Create simplified company object
            JSONObject simplified = new JSONObject();
            simplified.put("name", company.optString("name", ""));
            simplified.put("operation", operation);
            simplified.put("id", company.optString("id", ""));
            
            // Add to simplified companies
            simplifiedCompanies.put(simplified);
        }
        
        // Convert sets to arrays
        JSONArray sectorsArray = new JSONArray();
        sectors.forEach(sectorsArray::put);
        
        JSONArray operationsArray = new JSONArray();
        operationTypes.forEach(operationsArray::put);
        
        // Add to result
        result.put("company_sectors", sectorsArray);
        result.put("operation_types", operationsArray);
        result.put("companies", simplifiedCompanies);
        result.put("total_companies", companies.length());
    }
}