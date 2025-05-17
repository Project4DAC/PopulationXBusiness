//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.ulpgc.StoreBuilder.Commands;

import java.io.File;
import org.ulpgc.StoreBuilder.Interfaces.Command;

public class ListFilesCommand implements Command {
    public ListFilesCommand() {
    }

    public void execute() {
        System.out.println("\nFiles in DataLake:");
        String rootPath = System.getenv("DATALAKE_PATH") != null ? System.getenv("DATALAKE_PATH") : System.getProperty("user.home") + File.separator + "activemq-datalake";
        this.listFilesInDirectory(rootPath + File.separator + "raw", "Raw Zone");
        this.listFilesInDirectory(rootPath + File.separator + "processed", "Processed Zone");
        this.listFilesInDirectory(rootPath + File.separator + "consumption", "Consumption Zone");
    }

    private void listFilesInDirectory(String directory, String zoneName) {
        File dir = new File(directory);
        File[] files = dir.listFiles();
        System.out.println("\n" + zoneName + ":");
        if (files != null && files.length > 0) {
            File[] var5 = files;
            int var6 = files.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                File file = var5[var7];
                System.out.println("  - " + file.getName());
            }
        } else {
            System.out.println("  (No files)");
        }

    }

    public String getDescription() {
        return "Lists files in DataLake";
    }
}
