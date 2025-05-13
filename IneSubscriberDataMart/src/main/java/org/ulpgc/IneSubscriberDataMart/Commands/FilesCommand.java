package org.ulpgc.IneSubscriberDataMart.Commands;

import org.ulpgc.INESubscriberDataLake.Interfaces.Command;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesCommand implements Command {
    private final String datalakeRoot  = System.getenv("DATALAKE_PATH") != null ?
            System.getenv("DATALAKE_PATH") :
            System.getProperty("user.home") + File.separator + "activemq-datalake";
    private final List<String> zones = Arrays.asList("raw", "processed", "consumption");
    
    @Override
    public void execute() {
        System.out.println("INE DataLake File Listing:");
        
        // Verify if datalake directory exists
        File datalakeDir = new File(datalakeRoot);
        if (!datalakeDir.exists()) {
            System.out.println("DataLake directory not found. It will be created when the first file is saved.");
            return;
        }
        
        // List files in each zone
        for (String zone : zones) {
            Path zonePath = Paths.get(datalakeRoot, zone);
            if (Files.exists(zonePath)) {
                System.out.println("\n" + zone.toUpperCase() + " zone:");
                try (Stream<Path> paths = Files.list(zonePath)) {
                    List<Path> sortedPaths = paths
                            .filter(Files::isRegularFile)
                            .sorted(Comparator.comparing(Path::getFileName))
                            .collect(Collectors.toList());
                    
                    if (sortedPaths.isEmpty()) {
                        System.out.println("  No files found");
                    } else {
                        for (Path path : sortedPaths) {
                            System.out.println("  " + path.getFileName() + 
                                    " (" + Files.size(path) + " bytes)");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error listing files in " + zone + " zone: " + e.getMessage());
                }
            } else {
                System.out.println("\n" + zone.toUpperCase() + " zone: Directory not found");
            }
        }
    }

    @Override
    public String getDescription() {
        return "Lists files in the INE DataLake";
    }
}