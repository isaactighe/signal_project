package data_management;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;

import java.util.List;

class DataStorageTest {
    @Test
    void testAddAndGetRecords() {
        List<PatientRecord> mockData = List.of(
                new PatientRecord(1, 100.0, "WhiteBloodCells", 1714376789050L),
                new PatientRecord(1, 200.0, "WhiteBloodCells", 1714376789051L)
        );

        DataReader reader = new MockDataReader(mockData);
        DataStorage storage = new DataStorage();

        for (PatientRecord record : reader.read()) {
            storage.addPatientData(
                    record.getPatientId(),
                    record.getMeasurementValue(),
                    record.getRecordType(),
                    record.getTimestamp()
            );
        }

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }
}
