package com.alerts.strategies;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.alerts.Alert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class BloodPressureStrategy implements AlertStrategy {

    private final AlertFactory factory = new BloodPressureAlertFactory();

    @Override
    public List<Alert> checkAlert(Patient patient,
                                  List<PatientRecord> records) {

        List<Alert> alerts = new ArrayList<>();

        String patientId = String.valueOf(patient.getPatientId());

        List<PatientRecord> systolic = records.stream()
                .filter(r -> r.getRecordType().equals("SystolicBP"))
                .collect(Collectors.toList());

        List<PatientRecord> diastolic = records.stream()
                .filter(r -> r.getRecordType().equals("DiastolicBP"))
                .collect(Collectors.toList());

        // thresholds
        for (PatientRecord r : systolic) {

            if (r.getMeasurementValue() > 180
                    || r.getMeasurementValue() < 90) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Critical Systolic Blood Pressure",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "CRITICAL"
                );

                alerts.add(alert);
            }
        }

        for (PatientRecord r : diastolic) {

            if (r.getMeasurementValue() > 120
                    || r.getMeasurementValue() < 60) {

                Alert alert = factory.createAlert(
                        patientId,
                        "Critical Diastolic Blood Pressure",
                        r.getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "CRITICAL"
                );

                alerts.add(alert);
            }
        }

        alerts.addAll(
                evaluateTrend(
                        systolic,
                        patientId,
                        "Systolic BP Trend"
                )
        );

        alerts.addAll(
                evaluateTrend(
                        diastolic,
                        patientId,
                        "Diastolic BP Trend"
                )
        );

        return alerts;
    }

    private List<Alert> evaluateTrend(List<PatientRecord> records,
                                      String patientId,
                                      String condition) {

        List<Alert> alerts = new ArrayList<>();

        if (records.size() < 3) {
            return alerts;
        }

        for (int i = 0; i <= records.size() - 3; i++) {

            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();

            boolean increasing =
                    (v2 - v1 > 10 && v3 - v2 > 10);

            boolean decreasing =
                    (v1 - v2 > 10 && v2 - v3 > 10);

            if (increasing || decreasing) {

                Alert alert = factory.createAlert(
                        patientId,
                        condition,
                        records.get(i + 2).getTimestamp()
                );

                alert = new PriorityAlertDecorator(
                        alert,
                        "MEDIUM"
                );

                alert = new RepeatedAlertDecorator(
                        alert,
                        2
                );

                alerts.add(alert);
            }
        }

        return alerts;
    }
}