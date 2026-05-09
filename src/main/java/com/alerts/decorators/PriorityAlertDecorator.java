package com.alerts.decorators;

import com.alerts.Alert;

public class PriorityAlertDecorator extends AlertDecorator {

    private final String priority;

    public PriorityAlertDecorator(Alert alert, String priority) {
        super(alert);
        this.priority = priority;
    }

    public String getPriority() {
        return priority;
    }

    @Override
    public String getType() {
        return wrappedAlert.getType() + " | PRIORITY: " + priority;
    }
}