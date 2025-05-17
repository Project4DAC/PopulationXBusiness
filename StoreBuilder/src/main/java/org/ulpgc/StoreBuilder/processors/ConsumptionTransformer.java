//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.processors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;


public class ConsumptionTransformer implements DataTransformer {
    public ConsumptionTransformer() {
    }

    public String transform(String data) {
        JSONObject result;
        try {
            JSONObject json = new JSONObject(data);
            result = new JSONObject();
            result.put("consumption_generated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            result.put("data_source", "BORME");
            if (json.has("date") && json.has("notification_day")) {
                this.transformNotification(json, result);
            } else if (!json.has("companies") && !json.has("statistics")) {
                result.put("raw_data", json);
                result.put("transformation_status", "minimal");
            } else {
                this.transformContent(json, result);
            }

            return result.toString(2);
        } catch (Exception var4) {
            Exception e = var4;
            System.err.println("Error in consumption transformation: " + e.getMessage());
            result = new JSONObject();
            result.put("error", "Transformation failed");
            result.put("message", e.getMessage());
            result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            result.put("original_data_sample", data.length() > 100 ? data.substring(0, 100) + "..." : data);
            return result.toString(2);
        }
    }

    private void transformNotification(JSONObject json, JSONObject result) {
        String date = json.optString("date", "");
        String url = json.optString("url", "");
        JSONObject notification = new JSONObject();
        notification.put("publication_date", date);
        notification.put("source_url", url);
        notification.put("year", json.optString("notification_year", ""));
        notification.put("month", json.optString("notification_month", ""));
        notification.put("day", json.optString("notification_day", ""));
        notification.put("data_quality", json.optString("data_quality", "unknown"));
        result.put("notification", notification);
        result.put("document_type", "notification");
        result.put("ready_for_analytics", true);
    }

    private void transformContent(JSONObject json, JSONObject result) {
        result.put("document_type", "content");
        if (json.has("statistics")) {
            result.put("statistics", json.getJSONObject("statistics"));
        }

        if (json.has("companies")) {
            this.transformCompanies(json.getJSONArray("companies"), result);
        }

        result.put("ready_for_analytics", true);
    }

    private void transformCompanies(JSONArray companies, JSONObject result) {
        Set<String> sectors = new HashSet();
        Set<String> operationTypes = new HashSet();
        JSONArray simplifiedCompanies = new JSONArray();

        for(int i = 0; i < companies.length(); ++i) {
            JSONObject company = companies.getJSONObject(i);
            String name = company.optString("name", "").toLowerCase();
            if (!name.contains("tech") && !name.contains("tecnolog")) {
                if (name.contains("consult")) {
                    sectors.add("consulting");
                } else if (!name.contains("financ") && !name.contains("inver")) {
                    if (name.contains("construc")) {
                        sectors.add("construction");
                    } else {
                        sectors.add("other");
                    }
                } else {
                    sectors.add("financial");
                }
            } else {
                sectors.add("technology");
            }

            String operation = company.optString("operation", "unknown");
            operationTypes.add(operation);
            JSONObject simplified = new JSONObject();
            simplified.put("name", company.optString("name", ""));
            simplified.put("operation", operation);
            simplified.put("id", company.optString("id", ""));
            simplifiedCompanies.put(simplified);
        }

        JSONArray sectorsArray = new JSONArray();
        Objects.requireNonNull(sectorsArray);
        sectors.forEach(sectorsArray::put);
        JSONArray operationsArray = new JSONArray();
        Objects.requireNonNull(operationsArray);
        operationTypes.forEach(operationsArray::put);
        result.put("company_sectors", sectorsArray);
        result.put("operation_types", operationsArray);
        result.put("companies", simplifiedCompanies);
        result.put("total_companies", companies.length());
    }
}
