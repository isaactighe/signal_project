package com.alerts.factories;

import com.alerts.Alert;
import com.alerts.BasicAlert;
import com.alerts.HeartRateAlert;

public class HeartRateAlertFactory extends AlertFactory {

    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new HeartRateAlert(patientId, condition, timestamp);
    }
}