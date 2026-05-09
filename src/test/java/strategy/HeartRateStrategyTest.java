package strategy;

import com.alerts.AlertGenerator;
import com.alerts.strategies.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class HeartRateStrategyTest {

    private DataStorage dataStorage;
    private AlertGenerator alertGenerator;
    private Patient patient;

    @BeforeEach
    void setup() {
        dataStorage = DataStorage.getInstance();

        alertGenerator = new AlertGenerator(
                List.of(
                        new BloodPressureStrategy(),
                        new HeartRateStrategy(),
                        new OxygenSaturationStrategy()
                )
        );

        patient = new Patient(1);
    }

    @Test
    void testHighHeartRateTriggersAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 120, "HeartRate", t);

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testLowHeartRateTriggersAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 45, "HeartRate", t);

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testNormalHeartRateNoAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 70, "HeartRate", t);

        alertGenerator.evaluateData(patient);
    }
}