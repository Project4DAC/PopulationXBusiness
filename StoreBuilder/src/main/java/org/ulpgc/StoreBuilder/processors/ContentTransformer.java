//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.processors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;

public class ContentTransformer implements DataTransformer {
    public ContentTransformer() {
    }

    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            if (json.has("companies")) {
                JSONArray companies = json.getJSONArray("companies");
                json.put("company_count", companies.length());
                JSONObject companyStats = new JSONObject();
                int incorporations = 0;
                int dissolutions = 0;
                int modifications = 0;

                for(int i = 0; i < companies.length(); ++i) {
                    JSONObject company = companies.getJSONObject(i);
                    String operation = company.optString("operation", "unknown").toLowerCase();
                    if (operation.contains("constitu")) {
                        ++incorporations;
                    } else if (!operation.contains("disolu") && !operation.contains("liquid")) {
                        ++modifications;
                    } else {
                        ++dissolutions;
                    }
                }

                companyStats.put("incorporations", incorporations);
                companyStats.put("dissolutions", dissolutions);
                companyStats.put("modifications", modifications);
                json.put("statistics", companyStats);
            }

            return json.toString(2);
        } catch (Exception var11) {
            Exception e = var11;
            System.err.println("Error transforming content: " + e.getMessage());
            return data;
        }
    }
}
