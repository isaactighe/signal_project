package data_management;

import com.data_management.PatientRecord;

import java.util.List;

public interface DataReader {
    List<PatientRecord> read();
}
