package strategy;

import com.alerts.AlertGenerator;
import com.alerts.strategies.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class OxygenSaturationStrategyTest {

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
    void testLowOxygenTriggersAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 90, "Saturation", t);

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testNormalOxygenNoAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 97, "Saturation", t);

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testRapidDropTriggersAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 98, "Saturation", t);
        dataStorage.addPatientData(1, 92, "Saturation", t + 300000); // 5 min drop

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testDropOutsideTimeWindowNoAlert() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 98, "Saturation", t);
        dataStorage.addPatientData(1, 92, "Saturation", t + 700000); // >10 min

        alertGenerator.evaluateData(patient);
    }
}