package com.alerts.strategies;

import java.util.List;
import java.util.stream.Collectors;

import com.alerts.*;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.factories.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodOxygenAlertFactory();

    @Override
    public void checkAlert(Patient patient,
                           List<PatientRecord> records,
                           AlertGenerator generator) {

        String patientId = String.valueOf(patient.getPatientId());

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

                alert = new PriorityAlertDecorator(alert, "HIGH");

                generator.triggerAlert(alert);
            }
        }

        checkRapidDrop(patient, saturation, generator, patientId);
    }

    private void checkRapidDrop(Patient patient,
                                List<PatientRecord> saturation,
                                AlertGenerator generator,
                                String patientId) {

        long tenMinutes = 10 * 60 * 1000;

        for (int i = 0; i < saturation.size() - 1; i++) {

            PatientRecord current = saturation.get(i);
            PatientRecord next = saturation.get(i + 1);

            long diff = next.getTimestamp() - current.getTimestamp();
            double drop = current.getMeasurementValue() - next.getMeasurementValue();

            if (diff <= tenMinutes && drop >= 5) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Rapid Oxygen Saturation Drop",
                        next.getTimestamp()
                );

                alert = new PriorityAlertDecorator(alert, "CRITICAL");

                generator.triggerAlert(alert);
            }
        }
    }
}