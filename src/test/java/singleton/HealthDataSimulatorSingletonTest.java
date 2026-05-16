package singleton;

import com.cardio_generator.HealthDataSimulator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HealthDataSimulatorSingletonTest {

    @Test
    void testOnlyOneInstanceExists() {

        HealthDataSimulator a =
                HealthDataSimulator.getInstance();

        HealthDataSimulator b =
                HealthDataSimulator.getInstance();

        assertSame(a, b);
    }

    @Test
    void testStatePersistsAcrossCalls()
            throws Exception {

        HealthDataSimulator simulator =
                HealthDataSimulator.getInstance();

        Field field =
                HealthDataSimulator.class.getDeclaredField(
                        "patientCount"
                );

        field.setAccessible(true);

        field.set(simulator, 100);

        HealthDataSimulator sameSimulator =
                HealthDataSimulator.getInstance();

        int patientCount =
                (int) field.get(sameSimulator);

        assertEquals(100, patientCount);
    }
}