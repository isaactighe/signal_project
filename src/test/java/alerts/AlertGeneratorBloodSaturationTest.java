package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.strategies.AlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

class AlertGeneratorBloodSaturationTest {
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
    void testLowSaturationBelowThreshold() {
        // 91% is below the 92% danger threshold — a low saturation alert should fire
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testLowSaturationAtThreshold() {
        // 92% is right at the boundary — checks whether the threshold is inclusive or exclusive
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 92, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testNormalSaturation() {
        // 95% is well within the safe range — no alert should be raised
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropWithin10Minutes() {
        // a 5% drop from 98 to 93 within 5 minutes should trigger a rapid-drop alert
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 93, "Saturation", baseTime + 300000); // 5 min, 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropExactly5Percent() {
        // exactly 5% drop is the boundary case — verifies the threshold is inclusive
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 100, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime + 300000); // exactly 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropBeyond10Minutes() {
        // the same 5% drop but spread over 11 minutes — outside the detection window, no alert expected
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 93, "Saturation", baseTime + 660000); // 11 min, 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testSmallDropWithin10Minutes() {
        // only 4% drop in 5 minutes — not enough to clear the 5% threshold, no alert
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 94, "Saturation", baseTime + 300000); // 4% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropWith3Readings() {
        // the drop is spread across three readings but the total from first to last exceeds 5% within the window
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 96, "Saturation", baseTime + 180000); // 2% drop
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime + 300000); // 5% drop total
        alertGenerator.evaluateData(testPatient);
    }
}
