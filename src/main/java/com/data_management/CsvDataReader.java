package com.data_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CsvDataReader implements DataReader {
    private String filePath;

    public CsvDataReader(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // skip header row if present

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                int patientId         = Integer.parseInt(parts[0].trim());
                double measurementVal = Double.parseDouble(parts[1].trim());
                String recordType     = parts[2].trim();
                long timestamp        = Long.parseLong(parts[3].trim());

                dataStorage.addPatientData(patientId, measurementVal, recordType, timestamp);
            }
        }
    }
}