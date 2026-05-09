package com.alerts.strategies;

import java.util.List;

import com.alerts.*;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.factories.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public void checkAlert(Patient patient,
                           List<PatientRecord> records,
                           AlertGenerator generator) {

        String patientId = String.valueOf(patient.getPatientId());

        for (PatientRecord r : records) {
            if (!r.getRecordType().equals("HeartRate")) continue;

            if (r.getMeasurementValue() > 100 || r.getMeasurementValue() < 50) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Abnormal Heart Rate",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(alert, "HIGH");

                generator.triggerAlert(alert);
            }
        }
    }
}