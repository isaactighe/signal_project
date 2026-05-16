package com.alerts;

public class ECGAlert extends BasicAlert {

    public ECGAlert(
            String patientId,
            String condition,
            long timestamp
    ) {
        super(patientId, condition, timestamp);
    }

    @Override
    public String getType() {
        return "ECG Alert";
    }
}