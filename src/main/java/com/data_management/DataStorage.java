package com.data_management;

import java.util.*;

public class DataStorage {

    private static DataStorage instance;

    private final Map<Integer, Patient> patientMap;

    private DataStorage() {
        this.patientMap = new HashMap<>();
    }

    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    public void addPatientData(int patientId, double value, String type, long timestamp) {
        Patient patient = patientMap.computeIfAbsent(patientId, Patient::new);
        patient.addRecord(value, type, timestamp);
    }

    public List<PatientRecord> getRecords(int patientId, long start, long end) {
        Patient p = patientMap.get(patientId);
        if (p == null) return new ArrayList<>();
        return p.getRecords(start, end);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }
}