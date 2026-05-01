package alerts;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

class AlertGeneratorManualAlertTest {
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
    void testManualAlertTriggered() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testMultipleManualAlerts() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime + 30000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testManualAlertWithOtherData() {
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }
}
