package com.alerts.decorators;

import com.alerts.Alert;

public class RepeatedAlertDecorator extends AlertDecorator {

    private final int repeatCount;

    public RepeatedAlertDecorator(Alert alert, int repeatCount) {
        super(alert);
        this.repeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    @Override
    public String getType() {
        return wrappedAlert.getType() + " | REPEATED x" + repeatCount;
    }
}