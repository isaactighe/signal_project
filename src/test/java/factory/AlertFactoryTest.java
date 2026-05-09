package factory;

import com.alerts.*;
import com.alerts.factories.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertFactoryTest {

    @Test
    void testBloodPressureFactoryCreatesCorrectType() {
        AlertFactory factory = new BloodPressureAlertFactory();

        Alert alert = factory.createAlert(
                "1",
                "BP issue",
                System.currentTimeMillis()
        );

        assertTrue(alert instanceof BasicAlert);
    }

    @Test
    void testHeartRateFactoryCreatesCorrectType() {
        AlertFactory factory = new HeartRateAlertFactory();

        Alert alert = factory.createAlert(
                "1",
                "HR issue",
                System.currentTimeMillis()
        );

        assertTrue(alert instanceof BasicAlert);
    }

    @Test
    void testECGFactoryCreatesCorrectType() {
        AlertFactory factory = new ECGAlertFactory();

        Alert alert = factory.createAlert(
                "1",
                "ECG issue",
                System.currentTimeMillis()
        );

        assertTrue(alert instanceof BasicAlert);
    }
}