package com.alerts.strategies;

import java.util.List;
import java.util.stream.Collectors;

import com.alerts.*;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.alerts.factories.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public void checkAlert(Patient patient,
                           List<PatientRecord> records,
                           AlertGenerator generator) {

        String patientId = String.valueOf(patient.getPatientId());

        List<PatientRecord> systolic = records.stream()
                .filter(r -> r.getRecordType().equals("SystolicBP"))
                .collect(Collectors.toList());

        List<PatientRecord> diastolic = records.stream()
                .filter(r -> r.getRecordType().equals("DiastolicBP"))
                .collect(Collectors.toList());

        // thresholds
        for (PatientRecord r : systolic) {
            if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Critical Systolic Blood Pressure",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(alert, "CRITICAL");

                generator.triggerAlert(alert);
            }
        }

        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Critical Diastolic Blood Pressure",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(alert, "CRITICAL");

                generator.triggerAlert(alert);
            }
        }

        evaluateTrend(patient, systolic, generator, patientId, "Systolic BP Trend");
        evaluateTrend(patient, diastolic, generator, patientId, "Diastolic BP Trend");
    }

    private void evaluateTrend(Patient patient,
                               List<PatientRecord> records,
                               AlertGenerator generator,
                               String patientId,
                               String condition) {

        if (records.size() < 3) return;

        for (int i = 0; i <= records.size() - 3; i++) {

            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();

            boolean increasing = (v2 - v1 > 10 && v3 - v2 > 10);
            boolean decreasing = (v1 - v2 > 10 && v2 - v3 > 10);

            if (increasing || decreasing) {

                Alert alert = factory.createAlert(
                        patientId,
                        condition,
                        records.get(i + 2).getTimestamp()
                );

                alert = new PriorityAlertDecorator(alert, "MEDIUM");
                alert = new RepeatedAlertDecorator(alert, 2);

                generator.triggerAlert(alert);
            }
        }
    }
}