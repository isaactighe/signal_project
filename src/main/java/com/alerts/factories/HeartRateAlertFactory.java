package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BasicAlert;

public class HeartRateAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new BasicAlert(patientId, condition, timestamp);
    }
}