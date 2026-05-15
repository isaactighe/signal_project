package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.strategies.AlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.util.List;

class AlertGeneratorManualAlertTest {
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
    void testManualAlertTriggered() {
        // a single manually-triggered alert record (value = 1) should be detected by the generator
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testMultipleManualAlerts() {
        // two manual alerts fired at different times — both should be picked up in the same evaluation pass
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime + 30000);
        alertGenerator.evaluateData(testPatient);
    }

    @Test
    void testManualAlertWithOtherData() {
        // manual alert is mixed in with normal vitals — checks that the generator doesn't miss it
        // when other record types are present in the same evaluation window
        long baseTime = System.currentTimeMillis();
        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", baseTime);
        dataStorage.addPatientData(PATIENT_ID, 1, "Alert", baseTime + 5000);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", baseTime + 10000);
        alertGenerator.evaluateData(testPatient);
    }
}
