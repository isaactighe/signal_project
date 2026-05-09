package com.alerts.strategies;

import com.alerts.AlertGenerator;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public interface AlertStrategy {
    void checkAlert(Patient patient,
                    List<PatientRecord> records,
                    AlertGenerator generator);
}
