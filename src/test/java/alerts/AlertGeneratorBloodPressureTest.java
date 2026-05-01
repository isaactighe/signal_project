package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorBloodPressureTest {
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
    void testIncreasingTrendSystolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 131, "SystolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 142, "SystolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testDecreasingTrendSystolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 160, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 149, "SystolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 138, "SystolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testIncreasingTrendDiastolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 70, "DiastolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 81, "DiastolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 92, "DiastolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testDecreasingTrendDiastolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 100, "DiastolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 89, "DiastolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 78, "DiastolicBP", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalHighSystolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 181, "SystolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalLowSystolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalHighDiastolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 121, "DiastolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testCriticalLowDiastolic() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 59, "DiastolicBP", baseTime);
        alertGenerator.evaluateData(testPatient);
    }
}
