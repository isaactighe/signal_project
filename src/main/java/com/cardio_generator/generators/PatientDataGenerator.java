package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Interface for generating simulated patient health data.
 * Different implementations generate different types of health measurements
 * (heart rate, blood pressure, oxygen saturation, etc.).
 */
public interface PatientDataGenerator {
    /**
     * Generates health data for a patient and sends it to the output strategy.
     *
     * @param patientId the patient's ID to generate data for
     * @param outputStrategy where to send the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
