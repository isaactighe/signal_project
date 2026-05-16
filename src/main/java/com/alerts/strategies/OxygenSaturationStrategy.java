package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alerts.Alert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory factory =
            new BloodOxygenAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient,
                                  List<PatientRecord> records) {

        List<Alert> alerts = new ArrayList<>();

        String patientId =
                String.valueOf(patient.getPatientId());

        List<PatientRecord> saturation = records.stream()
                .filter(r -> r.getRecordType().equals("Saturation"))
                .collect(Collectors.toList());

        for (PatientRecord r : saturation) {

            if (r.getMeasurementValue() < 92) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Low Oxygen Saturation",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "HIGH"
                );

                alerts.add(alert);
            }
        }

        alerts.addAll(
                checkRapidDrop(
                        saturation,
                        patientId
                )
        );

        return alerts;
    }

    private List<Alert> checkRapidDrop(
            List<PatientRecord> saturation,
            String patientId) {

        List<Alert> alerts = new ArrayList<>();

        long tenMinutes = 10 * 60 * 1000;

        for (int i = 0; i < saturation.size() - 1; i++) {

            PatientRecord current = saturation.get(i);
            PatientRecord next = saturation.get(i + 1);

            long diff =
                    next.getTimestamp()
                            - current.getTimestamp();

            double drop =
                    current.getMeasurementValue()
                            - next.getMeasurementValue();

            if (diff <= tenMinutes && drop >= 5) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Rapid Oxygen Saturation Drop",
                        next.getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "CRITICAL"
                );

                alerts.add(alert);
            }
        }

        return alerts;
    }
}