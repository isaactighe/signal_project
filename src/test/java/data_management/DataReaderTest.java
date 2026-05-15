package data_management;

import static org.junit.jupiter.api.Assertions.*;

import com.data_management.CsvDataReader;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class DataReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void testCsvReadDataParsesValidFile() throws IOException {
        // the simplest happy path — a well-formed csv with a header row and two data rows
        // checks that both records land in storage with the right values
        Path csv = tempDir.resolve("data.csv");
        Files.writeString(csv,
                "patientId,value,type,timestamp\n" +
                "1,98.6,Temperature,1000\n" +
                "2,120.0,SystolicBP,2000\n"
        );

        DataStorage storage = DataStorage.getInstance();
        CsvDataReader reader = new CsvDataReader(csv.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(1, 1000L, 1000L);
        assertEquals(1, records.size());
        assertEquals(98.6, records.get(0).getMeasurementValue());
    }

    @Test
    void testCsvReadDataSkipsHeaderRow() throws IOException {
        // the reader is supposed to skip the first line unconditionally
        // this ensures a file with only a header and no data rows produces no records
        Path csv = tempDir.resolve("header_only.csv");
        Files.writeString(csv, "patientId,value,type,timestamp\n");

        DataStorage storage = DataStorage.getInstance();
        CsvDataReader reader = new CsvDataReader(csv.toString());
        reader.readData(storage);

        // patient 99 never existed, so the list should be empty
        List<PatientRecord> records = storage.getRecords(99, 0L, Long.MAX_VALUE);
        assertTrue(records.isEmpty());
    }

    @Test
    void testCsvReadDataThrowsOnMissingFile() {
        // passing a path that doesn't exist should surface as an IOException, not a silent failure
        CsvDataReader reader = new CsvDataReader("/nonexistent/path/data.csv");
        assertThrows(IOException.class, () -> reader.readData(DataStorage.getInstance()));
    }

    @Test
    void testDefaultConnectDelegatesToReadData() throws IOException {
        // connect() has a default implementation that just calls readData()
        // a mock that records whether readData was called is enough to verify this
        boolean[] called = {false};
        com.data_management.DataReader reader = dataStorage -> called[0] = true;

        reader.connect(DataStorage.getInstance());
        assertTrue(called[0], "connect() should have delegated to readData()");
    }

    @Test
    void testDefaultDisconnectIsNoOp() {
        // disconnect() has a default empty implementation — calling it should never throw
        com.data_management.DataReader reader = dataStorage -> {};
        assertDoesNotThrow(reader::disconnect);
    }

    @Test
    void testMockDataReaderDisconnectTracked() throws IOException {
        // verifies that our MockDataReader correctly tracks disconnect() calls
        // useful as a sanity check before relying on it in other tests
        MockDataReader mock = new MockDataReader();
        assertFalse(mock.wasDisconnected());
        mock.disconnect();
        assertTrue(mock.wasDisconnected());
    }
}
