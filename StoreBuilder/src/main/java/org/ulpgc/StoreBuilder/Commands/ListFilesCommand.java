package org.ulpgc.StoreBuilder.Commands;

import org.ulpgc.StoreBuilder.Interfaces.Command;

import java.io.File;

public class ListFilesCommand implements Command {
        @Override
        public void execute() {
            System.out.println("\nFiles in DataLake:");

            String rootPath = System.getenv("DATALAKE_PATH") != null ?
                    System.getenv("DATALAKE_PATH") :
                    System.getProperty("user.home") + File.separator + "activemq-datalake";

            listFilesInDirectory(rootPath + File.separator + "raw", "Raw Zone");
            listFilesInDirectory(rootPath + File.separator + "processed", "Processed Zone");
            listFilesInDirectory(rootPath + File.separator + "consumption", "Consumption Zone");
        }

        private void listFilesInDirectory(String directory, String zoneName) {
            File dir = new File(directory);
            File[] files = dir.listFiles();

            System.out.println("\n" + zoneName + ":");
            if (files != null && files.length > 0) {
                for (File file : files) {
                    System.out.println("  - " + file.getName());
                }
            } else {
                System.out.println("  (No files)");
            }
        }

        @Override
        public String getDescription() {
            return "Lists files in DataLake";
        }
    }
