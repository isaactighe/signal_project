package data_management;

import com.data_management.DataReader;
import com.data_management.DataStorage;

import java.io.IOException;

// a minimal in-memory reader used in tests to avoid touching real files or sockets
public class MockDataReader implements DataReader {

    private final Runnable onRead;
    private boolean disconnected = false;

    public MockDataReader(Runnable onRead) {
        this.onRead = onRead;
    }

    public MockDataReader() {
        this.onRead = () -> {};
    }

    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        onRead.run();
    }

    public boolean wasDisconnected() {
        return disconnected;
    }

    @Override
    public void disconnect() {
        disconnected = true;
    }
}
