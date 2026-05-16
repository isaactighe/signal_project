package decorator;

import com.alerts.Alert;
import com.alerts.BloodOxygenAlert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {

    @Test
    void testPriorityDecoratorDoesNotBreakAlert() {

        Alert alert = new BloodOxygenAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new PriorityAlertDecorator(alert, "HIGH");

        assertNotNull(alert);
        assertEquals("Test condition", alert.getCondition());
    }

    @Test
    void testRepeatedDecoratorDoesNotBreakAlert() {

        Alert alert = new BloodOxygenAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new RepeatedAlertDecorator(alert, 3);

        assertNotNull(alert);
        assertEquals("Test condition", alert.getCondition());
    }

    @Test
    void testDecoratorStillPreservesOutput() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Alert alert = new BloodOxygenAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new PriorityAlertDecorator(alert, "HIGH");

        System.out.println(alert.getCondition());

        assertTrue(out.toString().contains("Test condition"));
    }
}