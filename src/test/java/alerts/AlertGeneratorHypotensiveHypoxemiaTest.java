package alerts;

import static org.junit.jupiter.api.Assertions.*;

import com.alerts.strategies.AlertStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

class AlertGeneratorHypotensiveHypoxemiaTest {

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

    private String captureOutput(Runnable task) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        System.setOut(new PrintStream(out));

        try {
            task.run();
        } finally {
            System.setOut(originalOut);
        }

        return out.toString();
    }

    @Test
    void testHypotensiveHypoxemiaBothConditions() {
        // both systolic bp below 90 and saturation below 92% are present — the combined alert must fire
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertTrue(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaLowBPOnly() {
        // low bp alone is not enough — saturation is fine, so the combined alert should not appear
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 95, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertFalse(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaLowSaturationOnly() {
        // low saturation alone is not enough — bp is normal, so the combined alert should not appear
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 110, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertFalse(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaNormalBoth() {
        // both readings are comfortably within safe ranges — no alert of any kind expected
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 120, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 97, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertFalse(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaAtBoundaryBoth() {
        // bp at 89 (just below 90) and saturation at 91 (just below 92) — both cross the line, alert fires
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertTrue(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaAtBoundaryBPNotSaturation() {
        // bp at 89 triggers the bp side, but saturation at 92 is right at the threshold and should not
        // — this checks the exact boundary behaviour of the saturation check
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 89, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 92, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertFalse(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaMultipleReadings() {
        // multiple readings over time where at least one pair meets both conditions simultaneously
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 85, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 88, "SystolicBP", time + 5000);
        dataStorage.addPatientData(PATIENT_ID, 90, "Saturation", time);
        dataStorage.addPatientData(PATIENT_ID, 91, "Saturation", time + 5000);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertTrue(output.contains("Hypotensive Hypoxemia"));
    }

    @Test
    void testHypotensiveHypoxemiaCriticalValues() {
        // both values are well beyond the danger threshold — a clear-cut case that must always trigger
        long time = System.currentTimeMillis();

        dataStorage.addPatientData(PATIENT_ID, 70, "SystolicBP", time);
        dataStorage.addPatientData(PATIENT_ID, 85, "Saturation", time);

        String output = captureOutput(() ->
                alertGenerator.evaluateData(testPatient)
        );

        assertTrue(output.contains("Hypotensive Hypoxemia"));
    }
}
