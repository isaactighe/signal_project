package com.cardio_generator.generators;

import java.util.Random;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Simulates alert states for patients based on probabilistic triggers.
 * Tracks whether each patient has an active alert and transitions between
 * triggered and resolved states. Uses a Poisson-like distribution to decide
 * when alerts occur.
 */
public class AlertStateGenerator implements PatientDataGenerator {
    // random generator should not be public
    // should also user upper snake case:
    private static final Random RANDOM_GENERATOR = new Random();
    // AlertStates must be lower camel case
    /** false = resolved, true = triggered */
    private boolean[] alertStates;

    /**
     * Initializes the alert generator with all patients starting in resolved state.
     *
     * @param patientCount the total number of patients to track
     */
    public AlertStateGenerator(int patientCount) {
        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates or updates the alert state for a patient.
     * If an alert is already triggered, there's a 90% chance it resolves.
     * If no alert is active, uses probability to decide if a new one triggers.
     *
     * @param patientId the patient's ID
     * @param outputStrategy where to send the alert status update
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // lambda should be lower camel case
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
