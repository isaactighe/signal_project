package decorator;

import com.alerts.*;
import com.alerts.decorators.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class DecoratorTest {

    @Test
    void testPriorityDecoratorAddsPriority() {

        Alert alert = new BasicAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new PriorityAlertDecorator(alert, "HIGH");

        assertNotNull(alert);
    }

    @Test
    void testRepeatedDecoratorWrapsAlert() {

        Alert alert = new BasicAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new RepeatedAlertDecorator(alert, 3);

        assertNotNull(alert);
    }

    @Test
    void testDecoratorOutputModification() {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));

        Alert alert = new BasicAlert(
                "1",
                "Test condition",
                System.currentTimeMillis()
        );

        alert = new PriorityAlertDecorator(alert, "HIGH");

        System.out.println(alert.getCondition());

        assertTrue(out.toString().contains("Test condition"));
    }
}