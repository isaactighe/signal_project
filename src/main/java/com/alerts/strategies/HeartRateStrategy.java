package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;

import com.alerts.Alert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.HeartRateAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory factory =
            new HeartRateAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient,
                                  List<PatientRecord> records) {

        List<Alert> alerts = new ArrayList<>();

        String patientId =
                String.valueOf(patient.getPatientId());

        for (PatientRecord r : records) {

            if (!r.getRecordType().equals("HeartRate")) {
                continue;
            }

            if (r.getMeasurementValue() > 100
                    || r.getMeasurementValue() < 50) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Abnormal Heart Rate",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "HIGH"
                );

                alerts.add(alert);
            }
        }

        return alerts;
    }
}