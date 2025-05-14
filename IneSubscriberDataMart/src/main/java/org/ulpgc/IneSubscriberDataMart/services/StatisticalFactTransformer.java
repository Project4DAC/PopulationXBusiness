package org.ulpgc.IneSubscriberDataMart.services;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataTransformer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StatisticalFactTransformer implements DataTransformer {
    private final String dimensionsPath;

    public StatisticalFactTransformer() {
        // Use DATAMART_PATH consistently
        String dataMartRoot = System.getenv("DATAMART_PATH") != null ?
                System.getenv("DATAMART_PATH") :
                System.getProperty("user.home") + File.separator + "activemq-datamart";

        this.dimensionsPath = dataMartRoot + File.separator + "dimensions";
    }

    @Override
    public String transform(String data) {
        try {
            JSONObject inputJson = new JSONObject(data);
            JSONObject factTable = new JSONObject();

            // Generate fact table ID
            String factId = UUID.randomUUID().toString().substring(0, 8);
            factTable.put("fact_id", factId);

            // Link to dimension tables
            String datasetId = inputJson.optString("datasetId", "unknown");
            factTable.put("dataset_dimension_id", findDimensionId(datasetId));

            // Add fact date info
            String date = inputJson.optString("date", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE));
            factTable.put("fact_date", date);

            // Process the statistical data points into measures
            if (inputJson.has("data")) {
                JSONArray statisticalData = inputJson.getJSONArray("data");
                JSONArray measures = new JSONArray();

                // Aggregate statistics
                double sum = 0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                int numericCount = 0;
                Set<String> categories = new HashSet<>();

                for (int i = 0; i < statisticalData.length(); i++) {
                    JSONObject dataPoint = statisticalData.getJSONObject(i);
                    JSONObject measure = new JSONObject();

                    // Generate measure ID
                    measure.put("measure_id", UUID.randomUUID().toString().substring(0, 8));

                    // Add measure metadata from data point
                    if (dataPoint.has("category")) {
                        measure.put("category", dataPoint.getString("category"));
                        categories.add(dataPoint.getString("category"));
                    }

                    if (dataPoint.has("region")) {
                        measure.put("region", dataPoint.getString("region"));
                    }

                    if (dataPoint.has("period")) {
                        measure.put("period", dataPoint.getString("period"));
                    }

                    // Add the actual measure value
                    if (dataPoint.has("value")) {
                        try {
                            double value = dataPoint.getDouble("value");
                            measure.put("value", value);

                            // Update aggregates
                            sum += value;
                            min = Math.min(min, value);
                            max = Math.max(max, value);
                            numericCount++;
                        } catch (Exception e) {
                            // Handle non-numeric values
                            measure.put("value", dataPoint.get("value"));
                            measure.put("value_type", "non_numeric");
                        }
                    }

                    measures.put(measure);
                }

                // Add the measures to the fact table
                factTable.put("measures", measures);

                // Add aggregate statistics
                JSONObject aggregates = new JSONObject();
                if (numericCount > 0) {
                    aggregates.put("avg_value", sum / numericCount);
                    aggregates.put("min_value", min);
                    aggregates.put("max_value", max);
                    aggregates.put("count", numericCount);
                    aggregates.put("category_count", categories.size());
                }

                factTable.put("aggregates", aggregates);
            }

            // Add processing metadata
            factTable.put("processed_at", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
            factTable.put("etl_version", "1.0");

            return factTable.toString(2); // Pretty print with 2-space indentation
        } catch (Exception e) {
            System.err.println("Error transforming to fact table: " + e.getMessage());
            e.printStackTrace();
            return data; // Return original if processing fails
        }
    }

    private String findDimensionId(String datasetId) {
        // Search for the matching dimension file based on dataset ID
        try {
            File dimensionDir = new File(dimensionsPath);
            if (!dimensionDir.exists()) {
                return "unknown";
            }

            File[] files = dimensionDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String content = new String(Files.readAllBytes(file.toPath()));
                        JSONObject dimension = new JSONObject(content);

                        if (dimension.has("dataset_id") &&
                                dimension.getString("dataset_id").equals(datasetId)) {
                            return dimension.getString("dimension_id");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding dimension ID: " + e.getMessage());
        }

        return "unknown";
    }
}