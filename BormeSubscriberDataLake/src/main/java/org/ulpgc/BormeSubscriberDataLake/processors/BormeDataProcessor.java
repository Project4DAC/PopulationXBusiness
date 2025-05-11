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
        String filename = filePath.getFileName().toString();
        Path processedFile = Paths.get(processedPath, filename);

        // SKIP logic: omit if already processed
        if (Files.exists(processedFile)) {
            System.out.println("Skipping already processed file: " + filename);
            return false;
        }

        try {
            // Read the raw file
            String content = new String(Files.readAllBytes(filePath));

            // Select appropriate transformer
            DataTransformer transformer;
            String processedContent;
            String consumptionContent;

            if (filename.startsWith("notification_")) {
                transformer = transformers.get("notification");
                processedContent = transformer.transform(content);
            } else if (filename.startsWith("borme_content_")) {
                transformer = transformers.get("content");
                processedContent = transformer.transform(content);
            } else {
                // Unknown file type: passthrough
                processedContent = content;
            }

            // Save processed output
            saveToFile(processedContent, Paths.get(processedPath, filename));
            System.out.println("Processed file: " + filename);

            // Transform for consumption
            consumptionContent = transformers.get("consumption").transform(processedContent);
            String consumptionFilename = "consumption_" + filename;
            saveToFile(consumptionContent, Paths.get(consumptionPath, consumptionFilename));
            System.out.println("Created consumption version: " + consumptionFilename);

            return true;
        } catch (IOException e) {
            System.err.println("Error processing file " + filename + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void saveToFile(String content, Path target) throws IOException {
        File file = target.toFile();
        // Ensure parent directory exists
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
