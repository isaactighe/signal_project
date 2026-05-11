package com.data_management;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// central store for all patient records
// singleton so every part of the system shares the same data
public class DataStorage {

    private static DataStorage instance;

    // ConcurrentHashMap instead of HashMap because websocket messages arrive on multiple threads at once
    private final Map<Integer, Patient> patientMap;

    private DataStorage() {
        this.patientMap = new ConcurrentHashMap<>();
    }

    // one instance for the whole app — creates it on first call
    public static synchronized DataStorage getInstance() {
        if (instance == null) {
            instance = new DataStorage();
        }
        return instance;
    }

    // adds a single measurement for a patient
    // if this is the first record for that id, a new Patient is created automatically
    public void addPatientData(int patientId, double value, String type, long timestamp) {
        Patient patient = patientMap.computeIfAbsent(patientId, Patient::new);
        patient.addRecord(value, type, timestamp);
    }

    // returns all records for a patient that fall within the given time window
    public List<PatientRecord> getRecords(int patientId, long start, long end) {
        Patient p = patientMap.get(patientId);
        if (p == null) return new ArrayList<>();
        return p.getRecords(start, end);
    }

    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientMap.values());
    }
}
