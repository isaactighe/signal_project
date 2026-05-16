package com.alerts;

public class BloodPressureAlert extends BasicAlert {

    public BloodPressureAlert(
            String patientId,
            String condition,
            long timestamp
    ) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getType() {
        return "Blood Pressure Alert";
    }
}