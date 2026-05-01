package com.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private final DataStorage dataStorage;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (24 * 60 * 60 * 1000);
        List<PatientRecord> records = dataStorage.getRecords(patient.getPatientId(), startTime, endTime);
        checkBloodPressureAlerts(patient, records);
        checkHeartRateAlerts(patient, records);
        checkOxygenSaturationAlerts(patient, records);
        checkHypotensiveHypoxemia(patient, records);
        checkECGAlerts(patient, records);
        checkManualAlerts(patient, records);
    }

    private void checkOxygenSaturationAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> saturation = records.stream().filter(r -> r.getRecordType().equals("Saturation")).collect(Collectors.toList());

        for (PatientRecord r : saturation) {
            if (r.getMeasurementValue() < 92) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Low Oxygen Saturation", r.getTimestamp()));
            }
        }

        checkRapidSaturationDrop(patient, saturation);
    }

    private void checkRapidSaturationDrop(Patient patient, List<PatientRecord> saturation) {
        long tenMinutesMs = 10 * 60 * 1000;

        for (int i = 0; i < saturation.size() - 1; i++) {
            PatientRecord current = saturation.get(i);
            PatientRecord next = saturation.get(i + 1);

            long timeDiff = next.getTimestamp() - current.getTimestamp();
            double saturationDrop = current.getMeasurementValue() - next.getMeasurementValue();

            if (timeDiff <= tenMinutesMs && saturationDrop >= 5) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Rapid Oxygen Saturation Drop", next.getTimestamp()));
            }
        }
    }

    private void checkBloodPressureAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = records.stream().filter(r -> r.getRecordType().equals("SystolicBP")).collect(Collectors.toList());
        List<PatientRecord> diastolic = records.stream().filter(r -> r.getRecordType().equals("DiastolicBP")).collect(Collectors.toList());

        for (PatientRecord r : systolic) {
            if (r.getMeasurementValue() > 180 || r.getMeasurementValue() < 90) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Systolic Blood Pressure", r.getTimestamp()));
            }
        }
        for (PatientRecord r : diastolic) {
            if (r.getMeasurementValue() > 120 || r.getMeasurementValue() < 60) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Critical Diastolic Blood Pressure", r.getTimestamp()));
            }
        }

        evaluateTrend(patient, systolic, "Systolic Blood Pressure Trend");
        evaluateTrend(patient, diastolic, "Diastolic Blood Pressure Trend");

    }

    private void evaluateTrend(Patient patient, List<PatientRecord> specificRecords, String conditionName) {
        if (specificRecords.size() >= 3) {
            for (int i = 0; i <= specificRecords.size() - 3; i++) {

                double v1 = specificRecords.get(i).getMeasurementValue();
                double v2 = specificRecords.get(i+1).getMeasurementValue();
                double v3 = specificRecords.get(i+2).getMeasurementValue();

                boolean increasing = (v2 - v1 > 10 && v3 - v2 > 10);
                boolean decreasing = (v1 - v2 > 10 && v2 - v3 > 10);

                if ( increasing || decreasing) {
                    triggerAlert(new Alert(String.valueOf(patient.getPatientId()), conditionName, specificRecords.get(i+2).getTimestamp()));
                }
            }
        }
    }

    private void checkHeartRateAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> hrRecords = records.stream().filter(r -> r.getRecordType().equals("HeartRate")).collect(Collectors.toList());
        for (PatientRecord r : hrRecords) {
            if (r.getMeasurementValue() > 100 || r.getMeasurementValue() < 50) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal heart rate", r.getTimestamp()));
            }
        }
    }

    private void checkHypotensiveHypoxemia(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> systolic = records.stream().filter(r -> r.getRecordType().equals("SystolicBP")).collect(Collectors.toList());
        List<PatientRecord> saturation = records.stream().filter(r -> r.getRecordType().equals("Saturation")).collect(Collectors.toList());

        for (PatientRecord bpRecord : systolic) {
            if (bpRecord.getMeasurementValue() < 90) {
                for (PatientRecord satRecord : saturation) {
                    if (satRecord.getMeasurementValue() < 92) {
                        triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Hypotensive Hypoxemia Alert", satRecord.getTimestamp()));
                    }
                }
            }
        }
    }

    private void checkECGAlerts(Patient patient, List<PatientRecord> records) {
        List<PatientRecord> ecgRecords = new ArrayList<>();
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("ECG")) {
                ecgRecords.add(r);
            }
        }

        for (int i = 5; i < ecgRecords.size(); i++) {
            double avg = 0;
            for (int j = i - 5; j < i; j++) {
                avg += ecgRecords.get(j).getMeasurementValue();
            }
            avg /= 5;

            if (ecgRecords.get(i).getMeasurementValue() > avg * 1.5) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Abnormal ECG Peak", ecgRecords.get(i).getTimestamp()));
            }
        }
    }

    private void checkManualAlerts(Patient patient, List<PatientRecord> records) {
        for (PatientRecord r : records) {
            if (r.getRecordType().equals("Alert")) {
                triggerAlert(new Alert(String.valueOf(patient.getPatientId()), "Manual Alert Triggered", r.getTimestamp()));
            }
        }
    }

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        System.out.println("ALERT TRIGGERED: " + alert.getCondition() + " for Patient " + alert.getPatientId());
    }
}
