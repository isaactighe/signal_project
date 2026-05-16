package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BasicAlert;
import com.alerts.ECGAlert;

public class ECGAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new ECGAlert(patientId, condition, timestamp);
    }
}