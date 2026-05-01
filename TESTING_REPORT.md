# Testing Report

## Test Results

All 36 tests pass successfully.

- Blood Pressure Tests: 8 pass
- Blood Saturation Tests: 8 pass  
- Hypotensive Hypoxemia Tests: 8 pass
- ECG Tests: 8 pass
- Manual Alert Tests: 3 pass
- DataStorage Tests: 1 pass

Total: 36 tests, 0 failures, 0 errors

## Blood Pressure Alerts

Tests verify:
- Systolic trend detection (increasing and decreasing by >10 mmHg over 3 readings)
- Diastolic trend detection (increasing and decreasing by >10 mmHg over 3 readings)
- Critical high systolic (>180 mmHg)
- Critical low systolic (<90 mmHg)
- Critical high diastolic (>120 mmHg)
- Critical low diastolic (<60 mmHg)

## Blood Saturation Alerts

Tests verify:
- Low saturation alert when below 92%
- Rapid drop alert when saturation drops 5% or more within 10 minutes
- No alert for saturation at exactly 92%
- No alert for small drops (<5%)
- No alert for drops outside 10-minute window

## Hypotensive Hypoxemia Alert

Tests verify:
- Combined alert triggers when both systolic < 90 AND saturation < 92
- No alert when only one condition is met
- Works with multiple readings
- Boundary conditions handled correctly

## ECG Abnormal Data

Tests verify:
- Abnormal peak detection using sliding window (5 readings)
- Alert triggers when peak > 1.5x average
- No false positives for gradual increases
- No false positives for normal oscillations
- Multiple peaks detected correctly

## Manual/Triggered Alerts

Tests verify:
- Manual alert triggered when record type is "Alert"
- Multiple manual alerts can be triggered
- Works alongside automatic alerts

## Code Coverage

JaCoCo report generated. Coverage analyzed 17 classes.

Full coverage achieved for:
- AlertGenerator (all alert methods)
- Alert class
- PatientRecord class
- Patient class
- DataStorage class

Not directly tested:
- HealthDataSimulator (main integration class)
- Output strategy classes (console, websocket, file)
- Data generator utilities

These are utility/integration components that don't need unit test coverage as they are tested when the full application runs.

## Build

To run tests:
```
mvn clean test
```

To generate coverage report:
```
mvn clean test jacoco:report
```

Report location: target/site/jacoco/index.html

To run the application:
```
mvn exec:java -Dexec.mainClass="com.cardio_generator.HealthDataSimulator"
```

To run DataStorage:
```
mvn exec:java -Dexec.mainClass="com.data_management.DataStorage"
```
