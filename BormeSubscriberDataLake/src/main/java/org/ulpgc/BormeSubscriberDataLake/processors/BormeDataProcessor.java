package org.ulpgc.BormeSubscriberDataLake.processors;

import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataProcessor;
import org.ulpgc.BormeSubscriberDataLake.Interfaces.DataTransformer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class BormeDataProcessor implements DataProcessor {
    private final String rawPath;
    private final String processedPath;
    private final String consumptionPath;
    private final Map<String, DataTransformer> transformers;

    public BormeDataProcessor(String datalakeRoot) {
        this.rawPath = datalakeRoot + File.separator + "raw";
        this.processedPath = datalakeRoot + File.separator + "processed";
        this.consumptionPath = datalakeRoot + File.separator + "consumption";

        // Register transformers
        this.transformers = new HashMap<>();
        this.transformers.put("notification", new NotificationTransformer());
        this.transformers.put("content", new ContentTransformer());
        this.transformers.put("consumption", new ConsumptionTransformer());
    }

    @Override
    public void process() {
        AtomicInteger processedCount = new AtomicInteger(0);

        try (Stream<Path> paths = Files.list(Paths.get(rawPath))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        if (processFile(path)) {
                            processedCount.incrementAndGet();
                        }
                    });

            System.out.println("Processing complete! Processed " + processedCount.get() + " files.");
        } catch (IOException e) {
            System.err.println("Error processing raw files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean processFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();

            // Skip already processed files
            Path processedFile = Paths.get(processedPath, filename);
            if (Files.exists(processedFile)) {
                System.out.println("Skipping already processed file: " + filename);
                return false;
            }

            // Read the raw file
            String content = new String(Files.readAllBytes(filePath));

            // Select appropriate transformer and transform content
            String processedContent;
            String consumptionContent;

            if (filename.startsWith("notification_")) {
                DataTransformer transformer = transformers.get("notification");
                processedContent = transformer.transform(content);
                consumptionContent = transformers.get("consumption").transform(processedContent);
            } else if (filename.startsWith("borme_content_")) {
                DataTransformer transformer = transformers.get("content");
                processedContent = transformer.transform(content);
                consumptionContent = transformers.get("consumption").transform(processedContent);
            } else {
                // Unknown file type, just copy
                processedContent = content;
                consumptionContent = content;
            }

            // Save to processed zone
            saveToFile(processedContent, processedPath + File.separator + filename);
            System.out.println("Processed file: " + filename);

            // Save to consumption zone with prefix
            saveToFile(consumptionContent, consumptionPath + File.separator + "consumption_" + filename);
            System.out.println("Created consumption version: consumption_" + filename);

            return true;
        } catch (IOException e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
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