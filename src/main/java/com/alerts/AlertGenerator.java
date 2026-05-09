package com.alerts;

import java.util.List;

import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.alerts.strategies.AlertStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

public class AlertGenerator {

    private final List<AlertStrategy> strategies;

    public AlertGenerator(List<AlertStrategy> strategies) {
        this.strategies = strategies;
    }

    public void evaluateData(Patient patient) {

        long end = System.currentTimeMillis();
        long start = end - 86400000;

        List<PatientRecord> records =
                DataStorage.getInstance()
                        .getRecords(patient.getPatientId(), start, end);

        for (AlertStrategy s : strategies) {
            s.checkAlert(patient, records, this);
        }
    }

    public void triggerAlert(Alert alert) {


        alert = new PriorityAlertDecorator(alert, "HIGH");
        alert = new RepeatedAlertDecorator(alert, 3);

        System.out.println(
                "ALERT: " + alert.getCondition()
                        + " | " + alert.getType()
                        + " | Patient " + alert.getPatientId()
        );
    }
}