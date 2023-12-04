package com.berliz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DatabaseBackupScheduler {

    @Value("${BACKUP_PATH}")
    private String backupPath;

    @Scheduled(cron = "0 0 2 * * ?")  // Run daily at 2 AM
    public void backupDatabase() {
        try {
            String backupScriptPath = backupPath + "/backup-script.sh";
            ProcessBuilder processBuilder = new ProcessBuilder("bash", backupScriptPath);
            Process process = processBuilder.start();

            int exitCode = process.waitFor();

            // Handle the exit code as needed
            if (exitCode == 0) {
                System.out.println("Database backup completed successfully.");
            } else {
                System.err.println("Database backup failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
