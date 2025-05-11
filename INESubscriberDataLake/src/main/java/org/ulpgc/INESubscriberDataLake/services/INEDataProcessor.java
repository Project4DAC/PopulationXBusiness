package org.ulpgc.INESubscriberDataLake.services;

import org.ulpgc.INESubscriberDataLake.Interfaces.DataProcessor;
import org.ulpgc.INESubscriberDataLake.Interfaces.DataTransformer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class INEDataProcessor implements DataProcessor {
    private final String rawPath;
    private final String processedPath;
    private final String consumptionPath;
    private final Map<String, DataTransformer> transformers;

    public INEDataProcessor(String datalakeRoot) {
        this.rawPath = datalakeRoot + File.separator + "raw";
        this.processedPath = datalakeRoot + File.separator + "processed";
        this.consumptionPath = datalakeRoot + File.separator + "consumption";
        
        // Register transformers
        this.transformers = new HashMap<>();
        this.transformers.put("notification", new NotificationTransformer());
        this.transformers.put("data", new StatisticalDataTransformer());
    }

    @Override
    public void process() {
        try (Stream<Path> paths = Files.list(Paths.get(rawPath))) {
            paths.filter(Files::isRegularFile)
                 .forEach(this::processFile);
        } catch (IOException e) {
            System.err.println("Error processing raw INE files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            
            // Skip already processed files
            Path processedFile = Paths.get(processedPath, filename);
            if (Files.exists(processedFile)) {
                return;
            }
            
            // Read the raw file
            String content = new String(Files.readAllBytes(filePath));
            
            // Select appropriate transformer and transform content
            String processedContent;
            String consumptionContent;
            
            if (filename.startsWith("ine_notification_")) {
                DataTransformer transformer = transformers.get("notification");
                processedContent = transformer.transform(content);
                consumptionContent = createConsumptionVersion("notification", content, processedContent);
            } else if (filename.startsWith("ine_data_")) {
                DataTransformer transformer = transformers.get("data");
                processedContent = transformer.transform(content);
                consumptionContent = createConsumptionVersion("data", content, processedContent);
            } else {
                // Unknown file type, just copy
                processedContent = content;
                consumptionContent = content;
            }
            
            // Save to processed zone
            saveToFile(processedContent, processedPath + File.separator + filename);
            System.out.println("Processed INE file: " + filename);
            
            // Save to consumption zone
            saveToFile(consumptionContent, consumptionPath + File.separator + "consumption_" + filename);
            System.out.println("Created consumption version: consumption_" + filename);
            
        } catch (IOException e) {
            System.err.println("Error processing INE file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String createConsumptionVersion(String type, String rawContent, String processedContent) {
        // Create consumption-optimized version based on file type
        switch (type) {
            case "notification":
                return transformers.get("notification").transform(processedContent);
            case "data":
                return transformers.get("data").transform(processedContent);
            default:
                return processedContent;
        }
    }

    private void saveToFile(String content, String filepath) throws IOException {
        File file = new File(filepath);
        
        // Ensure directory exists
        file.getParentFile().mkdirs();
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}