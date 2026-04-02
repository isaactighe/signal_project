package com.cardio_generator.outputs;

/**
 * Interface for outputting patient health data to different destinations.
 * Different implementations can send data to console, files, TCP, WebSockets, etc.
 */
public interface OutputStrategy {
    /**
     * Sends patient health data to the output destination.
     *
     * @param patientId the patient's unique ID
     * @param timestamp when the measurement was taken (milliseconds since epoch)
     * @param label what type of measurement this is (e.g. "HeartRate", "Saturation")
     * @param data the actual measurement value as a string
     */
    void output(int patientId, long timestamp, String label, String data);
}
