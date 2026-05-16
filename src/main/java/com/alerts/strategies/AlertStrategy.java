package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.List;

public interface AlertStrategy {

    List<Alert> checkAlert(
            Patient patient,
            List<PatientRecord> records
    );
}