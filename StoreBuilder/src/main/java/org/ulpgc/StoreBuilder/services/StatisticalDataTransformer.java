
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;

public class StatisticalDataTransformer implements DataTransformer {
    public StatisticalDataTransformer() {
    }

    public String transform(String data) {
        try {
            JSONObject json = new JSONObject(data);
            json.put("processed_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            if (json.has("data")) {
                JSONArray statisticalData = json.getJSONArray("data");
                json.put("data_points", statisticalData.length());
                JSONObject stats = new JSONObject();
                double sum = 0.0;
                double min = Double.MAX_VALUE;
                double max = Double.MIN_VALUE;
                int numericCount = 0;

                for(int i = 0; i < statisticalData.length(); ++i) {
                    JSONObject dataPoint = statisticalData.getJSONObject(i);
                    if (dataPoint.has("value")) {
                        try {
                            double value = dataPoint.getDouble("value");
                            sum += value;
                            min = Math.min(min, value);
                            max = Math.max(max, value);
                            ++numericCount;
                        } catch (Exception var16) {
                        }
                    }
                }

                if (numericCount > 0) {
                    stats.put("mean", sum / (double)numericCount);
                    stats.put("min", min);
                    stats.put("max", max);
                    stats.put("count", numericCount);
                }

                json.put("statistics", stats);
            }

            return json.toString(2);
        } catch (Exception var17) {
            Exception e = var17;
            System.err.println("Error transforming statistical data: " + e.getMessage());
            return data;
        }
    }
}
