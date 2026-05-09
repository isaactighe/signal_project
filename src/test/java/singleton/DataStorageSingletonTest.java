package singleton;

import com.data_management.DataStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStorageSingletonTest {

    @Test
    void testOnlyOneInstanceExists() {
        DataStorage a = DataStorage.getInstance();
        DataStorage b = DataStorage.getInstance();

        assertSame(a, b);
    }

    @Test
    void testStatePersistsAcrossCalls() {
        DataStorage storage = DataStorage.getInstance();

        storage.addPatientData(1, 100, "HeartRate", System.currentTimeMillis());

        assertFalse(storage.getRecords(1, 0, System.currentTimeMillis()).isEmpty());
    }
}