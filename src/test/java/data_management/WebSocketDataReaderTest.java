package data_management;

import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// unit tests for WebSocketDataReader
// each test gets a fresh server on a random port and a clean DataStorage singleton
class WebSocketDataReaderTest {

    // field is set in each test so @AfterEach can always shut it down
    private StubServer server;

    @BeforeEach
    void resetStorage() throws Exception {
        // wipe the singleton between tests so records from one test don't bleed into the next
        Field f = DataStorage.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);
    }

    @AfterEach
    void stopServer() throws InterruptedException {
        if (server != null) {
            server.stop(500);
            server = null;
        }
    }

    // --- happy path ---

    @Test
    void testValidMessagesAreStoredCorrectly() throws Exception {
        server = startServer("42,1000,HeartRate,75.5", "42,2000,BloodPressure,120.0");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        List<PatientRecord> records = storage.getRecords(42, 0, Long.MAX_VALUE);
        assertEquals(2, records.size());
        assertEquals("HeartRate",     records.get(0).getRecordType());
        assertEquals(75.5,            records.get(0).getMeasurementValue(), 0.001);
        assertEquals("BloodPressure", records.get(1).getRecordType());
        assertEquals(120.0,           records.get(1).getMeasurementValue(), 0.001);
    }

    // spaces around each field should be stripped before parsing
    @Test
    void testWhitespaceAroundFieldsIsTrimmed() throws Exception {
        server = startServer(" 7 , 5000 , Saturation , 98.0 ");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        List<PatientRecord> records = storage.getRecords(7, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals("Saturation", records.get(0).getRecordType());
        assertEquals(98.0, records.get(0).getMeasurementValue(), 0.001);
    }

    // records for different patients must end up under their own ids, not mixed together
    @Test
    void testMultiplePatientsStoredUnderCorrectIds() throws Exception {
        server = startServer(
                "1,1000,HeartRate,60.0",
                "2,1000,HeartRate,80.0",
                "1,2000,SystolicBP,115.0"
        );

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertEquals(2, storage.getRecords(1, 0, Long.MAX_VALUE).size());
        assertEquals(1, storage.getRecords(2, 0, Long.MAX_VALUE).size());
        assertEquals(80.0, storage.getRecords(2, 0, Long.MAX_VALUE).get(0).getMeasurementValue(), 0.001);
    }

    // --- error handling ---

    // a message with no commas at all should be silently dropped
    @Test
    void testNoCommasSkipsMessage() throws Exception {
        server = startServer("this-is-not-a-valid-message");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertTrue(storage.getAllPatients().isEmpty());
    }

    // three fields is one short — the reader needs all four to do anything useful
    @Test
    void testTooFewFieldsSkipsMessage() throws Exception {
        server = startServer("42,1000,HeartRate");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertTrue(storage.getAllPatients().isEmpty());
    }

    // letters where a number is expected — should log the error and skip the message
    @Test
    void testNonNumericPatientIdSkipsMessage() throws Exception {
        server = startServer("abc,1000,HeartRate,75.5");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertTrue(storage.getAllPatients().isEmpty());
    }

    // value field is text instead of a number
    @Test
    void testNonNumericValueSkipsMessage() throws Exception {
        server = startServer("42,1000,HeartRate,seventy-five");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertTrue(storage.getAllPatients().isEmpty());
    }

    // timestamp field is not a number
    @Test
    void testNonNumericTimestampSkipsMessage() throws Exception {
        server = startServer("42,now,HeartRate,75.5");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        assertTrue(storage.getAllPatients().isEmpty());
    }

    // good messages should still be stored even if bad ones arrive in the same stream
    @Test
    void testValidAndInvalidMessagesMixed() throws Exception {
        server = startServer(
                "10,1000,HeartRate,65.0",   // valid
                "bad-message",               // no commas — skipped
                "10,2000,HeartRate,abc",     // bad value — skipped
                "10,3000,HeartRate,70.0"    // valid
        );

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        // only the two valid messages should be in storage
        assertEquals(2, storage.getRecords(10, 0, Long.MAX_VALUE).size());
    }

    // --- connection lifecycle ---

    // if the server closes the connection right after sending, the reader should not crash
    @Test
    void testServerClosingConnectionDoesNotThrow() throws Exception {
        server = new StubServer("42,1000,HeartRate,75.5") {
            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                super.onOpen(conn, handshake);
                // close immediately after sending — simulates a server that disconnects early
                conn.close();
            }
        };
        server.startAndWait();

        DataStorage storage = DataStorage.getInstance();
        assertDoesNotThrow(() -> connect(storage));
    }

    // calling disconnect before ever connecting should just do nothing
    @Test
    void testDisconnectOnInactiveReaderDoesNotThrow() throws URISyntaxException {
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:9999");
        assertDoesNotThrow(reader::disconnect);
    }

    // normal connect-then-disconnect should complete without errors
    @Test
    void testDisconnectAfterConnectClosesGracefully() throws Exception {
        server = startServer("42,1000,HeartRate,75.5");

        DataStorage storage = DataStorage.getInstance();
        WebSocketDataReader reader = readerFor(storage);
        server.waitForClient();

        assertDoesNotThrow(reader::disconnect);
    }

    // --- helpers ---

    // spins up a stub server on an os-chosen port — avoids port-already-in-use between tests
    private StubServer startServer(String... messages) throws InterruptedException {
        StubServer s = new StubServer(messages);
        s.startAndWait();
        return s;
    }

    // creates a reader pointed at the current server and connects it
    private void connect(DataStorage storage) throws Exception {
        new WebSocketDataReader("ws://localhost:" + server.getPort()).readData(storage);
    }

    // same as connect() but returns the reader so the test can call disconnect() on it
    private WebSocketDataReader readerFor(DataStorage storage) throws Exception {
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:" + server.getPort());
        reader.readData(storage);
        return reader;
    }

    // -------------------------------------------------------------------------

    // minimal websocket server that sends a fixed list of messages when a client connects
    private static class StubServer extends WebSocketServer {

        private final List<String> messages;
        // fires once the server socket is actually bound and accepting connections
        private final CountDownLatch startLatch  = new CountDownLatch(1);
        // fires once a client has connected and messages have been queued for sending
        private final CountDownLatch clientLatch = new CountDownLatch(1);

        StubServer(String... messages) {
            // port 0 tells the os to pick any free port — prevents conflicts between tests
            super(new InetSocketAddress(0));
            this.messages = Arrays.asList(messages);
            setReuseAddr(true);
        }

        // blocks until the server is fully started and ready to accept connections
        void startAndWait() throws InterruptedException {
            start();
            startLatch.await(5, TimeUnit.SECONDS);
        }

        // waits for a client to connect and then sleeps a bit for message delivery
        void waitForClient() throws InterruptedException {
            clientLatch.await(5, TimeUnit.SECONDS);
            // small sleep so the reader's onMessage callbacks finish processing
            Thread.sleep(300);
        }

        @Override
        public void onStart() {
            startLatch.countDown();
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            // push all messages then signal that the client is connected
            messages.forEach(conn::send);
            clientLatch.countDown();
        }

        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onMessage(WebSocket conn, String message) {}
        @Override public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }
    }
}
