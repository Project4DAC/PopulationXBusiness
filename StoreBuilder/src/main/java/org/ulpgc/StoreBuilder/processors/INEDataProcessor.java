//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.processors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.ulpgc.StoreBuilder.Interfaces.DataProcessor;
import org.ulpgc.StoreBuilder.Interfaces.DataTransformer;
import org.ulpgc.StoreBuilder.services.NotificationTransformer;
import org.ulpgc.StoreBuilder.services.StatisticalDataTransformer;

public class INEDataProcessor implements DataProcessor {
    private final String rawPath;
    private final String processedPath;
    private final String consumptionPath;
    private final Map<String, DataTransformer> transformers;

    public INEDataProcessor(String datalakeRoot) {
        this.rawPath = datalakeRoot + File.separator + "raw";
        this.processedPath = datalakeRoot + File.separator + "processed";
        this.consumptionPath = datalakeRoot + File.separator + "consumption";
        this.transformers = new HashMap();
        this.transformers.put("notification", new NotificationTransformer());
        this.transformers.put("data", new StatisticalDataTransformer());
    }

    public void process() {
        try {
            Stream<Path> paths = Files.list(Paths.get(this.rawPath));

            try {
                paths.filter((x$0) -> {
                    return Files.isRegularFile(x$0, new LinkOption[0]);
                }).forEach(this::processFile);
            } catch (Throwable var5) {
                if (paths != null) {
                    try {
                        paths.close();
                    } catch (Throwable var4) {
                        var5.addSuppressed(var4);
                    }
                }

                throw var5;
            }

            if (paths != null) {
                paths.close();
            }
        } catch (IOException var6) {
            IOException e = var6;
            System.err.println("Error processing raw INE files: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private void processFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            Path processedFile = Paths.get(this.processedPath, filename);
            if (Files.exists(processedFile, new LinkOption[0])) {
                return;
            }

            String content = new String(Files.readAllBytes(filePath));
            String processedContent;
            String consumptionContent;
            DataTransformer transformer;
            if (filename.startsWith("ine_notification_")) {
                transformer = (DataTransformer)this.transformers.get("notification");
                processedContent = transformer.transform(content);
                consumptionContent = this.createConsumptionVersion("notification", content, processedContent);
            } else if (filename.startsWith("ine_data_")) {
                transformer = (DataTransformer)this.transformers.get("data");
                processedContent = transformer.transform(content);
                consumptionContent = this.createConsumptionVersion("data", content, processedContent);
            } else {
                processedContent = content;
                consumptionContent = content;
            }

            this.saveToFile(processedContent, this.processedPath + File.separator + filename);
            System.out.println("Processed INE file: " + filename);
            this.saveToFile(consumptionContent, this.consumptionPath + File.separator + "consumption_" + filename);
            System.out.println("Created consumption version: consumption_" + filename);
        } catch (IOException var8) {
            IOException e = var8;
            PrintStream var10000 = System.err;
            String var10001 = String.valueOf(filePath);
            var10000.println("Error processing INE file " + var10001 + ": " + e.getMessage());
            e.printStackTrace();
        }

    }

    private String createConsumptionVersion(String type, String rawContent, String processedContent) {
        switch (type) {
            case "notification" -> {
                return ((DataTransformer)this.transformers.get("notification")).transform(processedContent);
            }
            case "data" -> {
                return ((DataTransformer)this.transformers.get("data")).transform(processedContent);
            }
            default -> {
                return processedContent;
            }
        }
    }

    private void saveToFile(String content, String filepath) throws IOException {
        File file = new File(filepath);
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
