package data_management;

import com.data_management.PatientRecord;
import java.util.List;

public class MockDataReader implements DataReader {
    private List<PatientRecord> mockRecords;

    public MockDataReader(List<PatientRecord> mockRecords) {
        this.mockRecords = mockRecords;
    }

    @Override
    public List<PatientRecord> read() {
        return mockRecords;
    }
}