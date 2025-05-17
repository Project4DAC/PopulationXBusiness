//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;

public class BormeDataProcessor implements DataProcessor {
    private final String rawPath;
    private final String processedPath;
    private final String consumptionPath;
    private final Map<String, DataTransformer> transformers;

    public BormeDataProcessor(String datalakeRoot) {
        this.rawPath = datalakeRoot + File.separator + "raw";
        this.processedPath = datalakeRoot + File.separator + "processed";
        this.consumptionPath = datalakeRoot + File.separator + "consumption";
        this.transformers = new HashMap();
        this.transformers.put("notification", new NotificationTransformer());
        this.transformers.put("content", new ContentTransformer());
        this.transformers.put("consumption", new ConsumptionTransformer());
    }

    public void process() {
        AtomicInteger processedCount = new AtomicInteger(0);

        try {
            Stream<Path> paths = Files.list(Paths.get(this.rawPath));

            try {
                paths.filter((x$0) -> {
                    return Files.isRegularFile(x$0, new LinkOption[0]);
                }).forEach((path) -> {
                    if (this.processFile(path)) {
                        processedCount.incrementAndGet();
                    }

                });
                System.out.println("Processing complete! Processed " + processedCount.get() + " files.");
            } catch (Throwable var6) {
                if (paths != null) {
                    try {
                        paths.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (paths != null) {
                paths.close();
            }
        } catch (IOException var7) {
            IOException e = var7;
            System.err.println("Error processing raw files: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private boolean processFile(Path filePath) {
        String filename = filePath.getFileName().toString();
        Path processedFile = Paths.get(this.processedPath, filename);
        if (Files.exists(processedFile, new LinkOption[0])) {
            System.out.println("Skipping already processed file: " + filename);
            return false;
        } else {
            try {
                String content = new String(Files.readAllBytes(filePath));
                DataTransformer transformer;
                String processedContent;
                if (filename.startsWith("notification_")) {
                    transformer = (DataTransformer)this.transformers.get("notification");
                    processedContent = transformer.transform(content);
                } else if (filename.startsWith("borme_content_")) {
                    transformer = (DataTransformer)this.transformers.get("content");
                    processedContent = transformer.transform(content);
                } else {
                    processedContent = content;
                }

                this.saveToFile(processedContent, Paths.get(this.processedPath, filename));
                System.out.println("Processed file: " + filename);
                String consumptionContent = ((DataTransformer)this.transformers.get("consumption")).transform(processedContent);
                String consumptionFilename = "consumption_" + filename;
                this.saveToFile(consumptionContent, Paths.get(this.consumptionPath, consumptionFilename));
                System.out.println("Created consumption version: " + consumptionFilename);
                return true;
            } catch (IOException var9) {
                IOException e = var9;
                System.err.println("Error processing file " + filename + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
    }

    private void saveToFile(String content, Path target) throws IOException {
        File file = target.toFile();
        file.getParentFile().mkdirs();
        FileWriter writer = new FileWriter(file);

        try {
            writer.write(content);
        } catch (Throwable var8) {
            try {
                writer.close();
            } catch (Throwable var7) {
                var8.addSuppressed(var7);
            }

            throw var8;
        }

        writer.close();
    }
}
