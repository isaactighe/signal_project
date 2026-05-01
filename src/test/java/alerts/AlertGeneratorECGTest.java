package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorECGTest {
    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private Patient testPatient;
    private static final int PATIENT_ID = 1;

    @BeforeEach
    void setUp() {
        dataStorage = new DataStorage();
        alertGenerator = new AlertGenerator(dataStorage);
        testPatient = new Patient(PATIENT_ID);
    }

    @Test
    void testNormalECGData() {
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0 + (i % 3), "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testAbnormalPeakAboveAverage() {
        long baseTime = System.currentTimeMillis();
        // Build stable baseline
        for (int i = 0; i < 5; i++) {
            dataStorage.addPatientData(PATIENT_ID, 75.0, "ECG", baseTime + (i * 1000));
        }
        // Add abnormal peak far above average
        dataStorage.addPatientData(PATIENT_ID, 150.0, "ECG", baseTime + (5 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testMultipleAbnormalPeaks() {
        long baseTime = System.currentTimeMillis();
        // Baseline
        for (int i = 0; i < 3; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0, "ECG", baseTime + (i * 1000));
        }
        // First abnormal peak
        dataStorage.addPatientData(PATIENT_ID, 140.0, "ECG", baseTime + (3 * 1000));

        // Return to baseline
        for (int i = 4; i < 7; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0, "ECG", baseTime + (i * 1000));
        }
        // Second abnormal peak
        dataStorage.addPatientData(PATIENT_ID, 145.0, "ECG", baseTime + (7 * 1000));

        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testGradualIncreaseNoAlert() {
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0 + i, "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testOscillatingNormalData() {
        long baseTime = System.currentTimeMillis();
        double[] values = {70, 75, 72, 74, 71, 73, 72, 74, 71, 75};
        for (int i = 0; i < values.length; i++) {
            dataStorage.addPatientData(PATIENT_ID, values[i], "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testSmallDeviation() {
        long baseTime = System.currentTimeMillis();
        // Baseline
        for (int i = 0; i < 5; i++) {
            dataStorage.addPatientData(PATIENT_ID, 80.0, "ECG", baseTime + (i * 1000));
        }
        // Small increase (not abnormal)
        dataStorage.addPatientData(PATIENT_ID, 95.0, "ECG", baseTime + (5 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testVeryHighPeak() {
        long baseTime = System.currentTimeMillis();
        // Normal baseline
        for (int i = 0; i < 4; i++) {
            dataStorage.addPatientData(PATIENT_ID, 65.0, "ECG", baseTime + (i * 1000));
        }
        // Extreme peak
        dataStorage.addPatientData(PATIENT_ID, 200.0, "ECG", baseTime + (4 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRecoveryAfterPeak() {
        long baseTime = System.currentTimeMillis();
        // Baseline
        for (int i = 0; i < 3; i++) {
            dataStorage.addPatientData(PATIENT_ID, 72.0, "ECG", baseTime + (i * 1000));
        }
        // Abnormal peak
        dataStorage.addPatientData(PATIENT_ID, 155.0, "ECG", baseTime + (3 * 1000));
        // Return to baseline
        for (int i = 4; i < 8; i++) {
            dataStorage.addPatientData(PATIENT_ID, 72.0, "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }
}
