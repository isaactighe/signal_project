package com.alerts;

public class BloodOxygenAlert extends BasicAlert {

    public BloodOxygenAlert(
            String patientId,
            String condition,
            long timestamp
    ) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getType() {
        return "Blood Oxygen Alert";
    }
}