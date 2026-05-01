package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorBloodSaturationTest {
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
    void testLowSaturationBelowThreshold() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testLowSaturationAtThreshold() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 92, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testNormalSaturation() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropWithin10Minutes() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 93, "Saturation", baseTime + 300000); // 5 min, 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropExactly5Percent() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 100, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime + 300000); // exactly 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropBeyond10Minutes() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 93, "Saturation", baseTime + 660000); // 11 min, 5% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testSmallDropWithin10Minutes() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 94, "Saturation", baseTime + 300000); // 4% drop
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testRapidDropWith3Readings() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 98, "Saturation", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 96, "Saturation", baseTime + 180000); // 2% drop
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", baseTime + 300000); // 5% drop total
        alertGenerator.evaluateData(testPatient);
    }
}
