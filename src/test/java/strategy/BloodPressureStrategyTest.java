package strategy;

import com.alerts.AlertGenerator;
import com.alerts.strategies.*;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class BloodPressureStrategyTest {

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
    void testCriticalSystolicHigh() {
        dataStorage.addPatientData(1, 190, "SystolicBP", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
    }

    @Test
    void testCriticalSystolicLow() {
        dataStorage.addPatientData(1, 80, "SystolicBP", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
    }

    @Test
    void testNormalSystolicNoAlert() {
        dataStorage.addPatientData(1, 120, "SystolicBP", System.currentTimeMillis());
        alertGenerator.evaluateData(patient);
    }

    @Test
    void testTrendIncreasing() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 100, "SystolicBP", t);
        dataStorage.addPatientData(1, 115, "SystolicBP", t + 1000);
        dataStorage.addPatientData(1, 130, "SystolicBP", t + 2000);

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testTrendDecreasing() {
        long t = System.currentTimeMillis();

        dataStorage.addPatientData(1, 130, "SystolicBP", t);
        dataStorage.addPatientData(1, 115, "SystolicBP", t + 1000);
        dataStorage.addPatientData(1, 100, "SystolicBP", t + 2000);

        alertGenerator.evaluateData(patient);
    }
}