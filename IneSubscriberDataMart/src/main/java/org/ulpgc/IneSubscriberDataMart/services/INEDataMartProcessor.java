package org.ulpgc.IneSubscriberDataMart.services;

import org.ulpgc.IneSubscriberDataMart.Interfaces.DataProcessor;
import org.ulpgc.IneSubscriberDataMart.Interfaces.DataTransformer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class INEDataMartProcessor implements DataProcessor {
    private final String stagingPath;
    private final String dimensionsPath;
    private final String factsPath;
    private final String reportsPath;
    private final Map<String, DataTransformer> transformers;

    public INEDataMartProcessor(String dataMartRoot) {
        this.stagingPath = dataMartRoot + File.separator + "staging";
        this.dimensionsPath = dataMartRoot + File.separator + "dimensions";
        this.factsPath = dataMartRoot + File.separator + "facts";
        this.reportsPath = dataMartRoot + File.separator + "reports";
        
        // Register transformers
        this.transformers = new HashMap<>();
        this.transformers.put("notification", new NotificationDimensionTransformer());
        this.transformers.put("data", new StatisticalFactTransformer());
        this.transformers.put("report", new ReportGenerator());
    }

    @Override
    public void process() {
        System.out.println("Processing INE data for Data Mart...");
        
        // 1. First process all notifications to build dimensions
        processNotificationDimensions();
        
        // 2. Then process data files to build facts
        processDataFactTables();
        
        // 3. Finally, generate reports
        generateReports();
        
        System.out.println("Data Mart processing complete.");
    }

    private void processNotificationDimensions() {
        try (Stream<Path> paths = Files.list(Paths.get(stagingPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().startsWith("ine_notification_"))
                 .forEach(this::processDimensionFile);
        } catch (IOException e) {
            System.err.println("Error processing notifications for dimensions: " + e.getMessage());
        }
    }
    
    private void processDataFactTables() {
        try (Stream<Path> paths = Files.list(Paths.get(stagingPath))) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.getFileName().toString().startsWith("ine_data_"))
                 .forEach(this::processFactFile);
        } catch (IOException e) {
            System.err.println("Error processing data for facts: " + e.getMessage());
        }
    }
    
    private void generateReports() {
        // Generate predefined reports from dimensions and facts
        DataTransformer reportGenerator = transformers.get("report");
        
        try {
            // Generate time-based statistical reports
            String timeReport = reportGenerator.transform("time_report");
            saveToFile(timeReport, reportsPath + File.separator + "time_based_statistics.json");
            
            // Generate dataset type summary report
            String datasetReport = reportGenerator.transform("dataset_report");
            saveToFile(datasetReport, reportsPath + File.separator + "dataset_type_summary.json");
            
            // Generate data quality report
            String qualityReport = reportGenerator.transform("quality_report");
            saveToFile(qualityReport, reportsPath + File.separator + "data_quality_analysis.json");
            
            System.out.println("Generated analytical reports in the Data Mart");
        } catch (IOException e) {
            System.err.println("Error generating reports: " + e.getMessage());
        }
    }

    private void processDimensionFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            
            // Skip already processed files
            String dimensionFilename = "dim_" + filename.replace("ine_notification_", "");
            Path dimensionFile = Paths.get(dimensionsPath, dimensionFilename);
            if (Files.exists(dimensionFile)) {
                return;
            }
            
            // Read the staging file
            String content = new String(Files.readAllBytes(filePath));
            
            // Transform notification into dimension
            DataTransformer transformer = transformers.get("notification");
            String dimensionContent = transformer.transform(content);
            
            // Save to dimensions zone
            saveToFile(dimensionContent, dimensionsPath + File.separator + dimensionFilename);
            System.out.println("Created dimension: " + dimensionFilename);
            
        } catch (IOException e) {
            System.err.println("Error processing dimension from " + filePath + ": " + e.getMessage());
        }
    }
    
    private void processFactFile(Path filePath) {
        try {
            String filename = filePath.getFileName().toString();
            
            // Skip already processed files
            String factFilename = "fact_" + filename.replace("ine_data_", "");
            Path factFile = Paths.get(factsPath, factFilename);
            if (Files.exists(factFile)) {
                return;
            }
            
            // Read the staging file
            String content = new String(Files.readAllBytes(filePath));
            
            // Transform data into fact
            DataTransformer transformer = transformers.get("data");
            String factContent = transformer.transform(content);
            
            // Save to facts zone
            saveToFile(factContent, factsPath + File.separator + factFilename);
            System.out.println("Created fact table: " + factFilename);
            
        } catch (IOException e) {
            System.err.println("Error processing fact from " + filePath + ": " + e.getMessage());
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