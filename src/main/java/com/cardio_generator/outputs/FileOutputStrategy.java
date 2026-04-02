package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outputs patient health data to separate files organized by measurement type.
 * Each measurement type (e.g., HeartRate, BloodPressure) gets its own file.
 * Multiple threads can safely write to this strategy thanks to the thread-safe file map.
 */
public class FileOutputStrategy implements OutputStrategy {

    private String BaseDirectory;

    /** Maps measurement labels to their file paths to avoid creating the same file multiple times */
    public final ConcurrentHashMap<String, String> file_map = new ConcurrentHashMap<>();

    /**
     * Creates a file output strategy that writes to the given directory.
     *
     * @param baseDirectory the directory where output files will be created
     */
    public FileOutputStrategy(String baseDirectory) {

        this.BaseDirectory = baseDirectory;
    }

    /**
     * Writes a patient measurement to a file.
     * Creates a separate file for each measurement label if it doesn't exist.
     *
     * @param patientId the patient's ID
     * @param timestamp when the measurement was taken
     * @param label the type of measurement (becomes the filename)
     * @param data the measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(BaseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Set the FilePath variable
        String FilePath = file_map.computeIfAbsent(label, k -> Paths.get(BaseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(FilePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + FilePath + ": " + e.getMessage());
        }
    }
}