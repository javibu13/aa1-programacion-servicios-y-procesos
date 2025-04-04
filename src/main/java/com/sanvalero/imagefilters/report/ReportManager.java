package com.sanvalero.imagefilters.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sanvalero.imagefilters.filter.Filter;

public class ReportManager {
    private File logFile;
    private final Lock lock = new ReentrantLock();
    
    public ReportManager() {
        this("logs/history_report.log");
    }

    public ReportManager(String logFilePath) {
        this.logFile = new File(logFilePath);
        // Verifica si el archivo ya existe
        if (logFile.exists()) {
            System.out.println("Log file already exists at: " + logFile.getAbsolutePath());
        } else {
            System.out.println("Log file does not exist. Creating a new one at: " + logFile.getAbsolutePath());
            // Crea el archivo si no existe
            try {
                if (logFile.createNewFile()) {
                    System.out.println("Log file created at: " + logFile.getAbsolutePath());
                }
            } catch (IOException e) {
                System.err.println("Failed to create log file: " + e.getMessage());
            }
        }
    }

    public void writeFilterReportEntry(LocalDateTime timestamp, String imagePath, List<Filter> filters) {
        lock.lock(); // Lock the method to prevent concurrent access
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            // Example format: 2023-10-01T12:00:00|path/to/image.jpg|Grayscale,Invert Colors
            String timestampedMessage = timestamp + "|" + imagePath + "|" + filters.toString()
                                                                                    .replace("[", "")
                                                                                    .replace("]", "");
            // Write the message to the log file
            writer.write(timestampedMessage);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing message: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public List<List<String>> readFilterReportEntries() {
        // Example: [["2023-10-01T12:00:00", "path/to/image.jpg", "Grayscale, Invert Colors"], ...]
        lock.lock(); // Lock the method to prevent concurrent access
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            List<List<String>> entries = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                List<String> entry = Arrays.asList(parts);
                entries.add(entry);
            }
            return entries;
        } catch (IOException e) {
            System.err.println("Error reading log file: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.unlock();
        }
    }
}
