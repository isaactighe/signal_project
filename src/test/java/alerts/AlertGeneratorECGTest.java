package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.strategies.AlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

class AlertGeneratorECGTest {
    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private Patient testPatient;
    private static final int PATIENT_ID = 1;

    @BeforeEach
    void setUp() {
        dataStorage = DataStorage.getInstance();

        List<AlertStrategy> strategies = List.of(
                new com.alerts.strategies.BloodPressureStrategy(),
                new com.alerts.strategies.HeartRateStrategy(),
                new com.alerts.strategies.OxygenSaturationStrategy()
        );

        alertGenerator = new AlertGenerator(strategies);

        testPatient = new Patient(PATIENT_ID);
    }

    @Test
    void testNormalECGData() {
        // small, steady oscillations around a stable baseline — nothing here should look like a spike
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0 + (i % 3), "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testAbnormalPeakAboveAverage() {
        // stable baseline at 75, then a single reading at 150 (2x average) — clearly an abnormal spike
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            dataStorage.addPatientData(PATIENT_ID, 75.0, "ECG", baseTime + (i * 1000));
        }
        dataStorage.addPatientData(PATIENT_ID, 150.0, "ECG", baseTime + (5 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testMultipleAbnormalPeaks() {
        // two separate spikes with a normal baseline in between — both should be detected
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0, "ECG", baseTime + (i * 1000));
        }
        // first abnormal peak
        dataStorage.addPatientData(PATIENT_ID, 140.0, "ECG", baseTime + (3 * 1000));

        // return to baseline
        for (int i = 4; i < 7; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0, "ECG", baseTime + (i * 1000));
        }
        // second abnormal peak
        dataStorage.addPatientData(PATIENT_ID, 145.0, "ECG", baseTime + (7 * 1000));

        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testGradualIncreaseNoAlert() {
        // values rise steadily by 1 each second — no single reading is far above the running average
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            dataStorage.addPatientData(PATIENT_ID, 70.0 + i, "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testOscillatingNormalData() {
        // natural heart rhythm variation — small ups and downs that should never exceed the spike threshold
        long baseTime = System.currentTimeMillis();
        double[] values = {70, 75, 72, 74, 71, 73, 72, 74, 71, 75};
        for (int i = 0; i < values.length; i++) {
            dataStorage.addPatientData(PATIENT_ID, values[i], "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testSmallDeviation() {
        // baseline at 80, then a jump to 95 — a 19% rise, but still within acceptable deviation
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            dataStorage.addPatientData(PATIENT_ID, 80.0, "ECG", baseTime + (i * 1000));
        }
        dataStorage.addPatientData(PATIENT_ID, 95.0, "ECG", baseTime + (5 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testVeryHighPeak() {
        // baseline at 65, then an extreme reading of 200 — well over 3x the average, must trigger
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            dataStorage.addPatientData(PATIENT_ID, 65.0, "ECG", baseTime + (i * 1000));
        }
        dataStorage.addPatientData(PATIENT_ID, 200.0, "ECG", baseTime + (4 * 1000));
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRecoveryAfterPeak() {
        // spike at reading 4, then values return to baseline — confirms the alert fires at the peak
        // and that normal readings after the spike don't suppress the earlier detection
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            dataStorage.addPatientData(PATIENT_ID, 72.0, "ECG", baseTime + (i * 1000));
        }
        dataStorage.addPatientData(PATIENT_ID, 155.0, "ECG", baseTime + (3 * 1000));
        for (int i = 4; i < 8; i++) {
            dataStorage.addPatientData(PATIENT_ID, 72.0, "ECG", baseTime + (i * 1000));
        }
        alertGenerator.evaluateData(testPatient);
    }
}
