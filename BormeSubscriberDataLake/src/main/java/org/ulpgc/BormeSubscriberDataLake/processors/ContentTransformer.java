package org.ulpgc.BormeSubscriberDataLake.processors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataTransformer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ContentTransformer implements DataTransformer {
    
    @Override
    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            
            // Add processing metadata
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            // Extract and analyze company data
            if (json.has("companies")) {
                JSONArray companies = json.getJSONArray("companies");
                json.put("company_count", companies.length());
                
                // Process each company to extract useful information
                JSONObject companyStats = new JSONObject();
                int incorporations = 0;
                int dissolutions = 0;
                int modifications = 0;
                
                for (int i = 0; i < companies.length(); i++) {
                    JSONObject company = companies.getJSONObject(i);
                    String operation = company.optString("operation", "unknown").toLowerCase();
                    
                    if (operation.contains("constitu")) {
                        incorporations++;
                    } else if (operation.contains("disolu") || operation.contains("liquid")) {
                        dissolutions++;
                    } else {
                        modifications++;
                    }
                }
                
                companyStats.put("incorporations", incorporations);
                companyStats.put("dissolutions", dissolutions);
                companyStats.put("modifications", modifications);
                json.put("statistics", companyStats);
            }
            
            return json.toString(2);
        } catch (Exception e) {
            System.err.println("Error transforming content: " + e.getMessage());
            return data; // Return original if processing fails
        }
    }
}