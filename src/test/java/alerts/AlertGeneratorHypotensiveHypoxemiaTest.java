package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorHypotensiveHypoxemiaTest {
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
    void testHypotensiveHypoxemiaBothConditions() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaLowBPOnly() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaLowSaturationOnly() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 110, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaNormalBoth() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 97, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaAtBoundariesBoth() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaAtBoundaryBPNotSaturation() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 92, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaMultipleReadings() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 88, "SystolicBP", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime + 5000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testHypotensiveHypoxemiaCriticalValues() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 70, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 85, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }
}
