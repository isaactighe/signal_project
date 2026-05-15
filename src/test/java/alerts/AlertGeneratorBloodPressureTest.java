package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.strategies.AlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

class AlertGeneratorBloodPressureTest {
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
    void testIncreasingTrendSystolic() {
        // three consecutive systolic readings each jumping more than 10 mmHg — the trend alert should fire
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 131, "SystolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 142, "SystolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testDecreasingTrendSystolic() {
        // three consecutive systolic readings each dropping more than 10 mmHg — the trend alert should fire
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 160, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 149, "SystolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 138, "SystolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testIncreasingTrendDiastolic() {
        // same trend logic but for diastolic — ensures the strategy covers both channels
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 70, "DiastolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 81, "DiastolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 92, "DiastolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testDecreasingTrendDiastolic() {
        // same as above but going down — mirrors the systolic decreasing test for diastolic
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 100, "DiastolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 89, "DiastolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 78, "DiastolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalHighSystolic() {
        // a single reading above 180 should trigger a critical threshold alert immediately
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 181, "SystolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalLowSystolic() {
        // a single reading below 90 is dangerously low and should trigger a critical alert
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalHighDiastolic() {
        // diastolic above 120 is a hypertensive crisis level — must trigger an alert
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 121, "DiastolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalLowDiastolic() {
        // diastolic below 60 indicates very low perfusion pressure — alert expected
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 59, "DiastolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }
}
