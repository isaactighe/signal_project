package data_management;

import com.alerts.AlertGenerator;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.data_management.DataStorage;
import com.data_management.Patient;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

// integration tests — exercise the full path: websocket server → reader → storage → alert generator
// each test starts a real server, sends real messages, and checks real storage
class WebSocketIntegrationTest {

    private StubServer server;
    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() throws Exception {
        // fresh storage for every test so records don't carry over
        Field f = DataStorage.class.getDeclaredField("instance");
        f.setAccessible(true);
        f.set(null, null);

        // all three strategies wired up, same as the real app
        alertGenerator = new AlertGenerator(List.of(
                new BloodPressureStrategy(),
                new HeartRateStrategy(),
                new OxygenSaturationStrategy()
        ));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (server != null) {
            server.stop(500);
            server = null;
        }
    }

    // smoke test — three different record types should all land in storage with the right patient id
    @Test
    void testRecordsReachDataStorage() throws Exception {
        server = startServer(
                "5,1000,HeartRate,72.0",
                "5,2000,SystolicBP,118.0",
                "5,3000,DiastolicBP,78.0"
        );

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        List<PatientRecord> records = storage.getRecords(5, 0, Long.MAX_VALUE);
        assertEquals(3, records.size());
        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("HeartRate")));
        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("SystolicBP")));
        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("DiastolicBP")));
    }

    // verifies that data arriving via websocket is in a state the alert generator can work with
    @Test
    void testAlertGeneratorRunsOnWebSocketData() throws Exception {
        server = startServer(
                "3,1000,HeartRate,65.0",
                "3,2000,SystolicBP,125.0",
                "3,3000,DiastolicBP,82.0"
        );

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        // normal values — no alerts expected, just checking it doesn't blow up
        assertDoesNotThrow(() -> alertGenerator.evaluateData(new Patient(3)));
    }

    // a systolic value above 180 should be stored intact and not cause any errors downstream
    @Test
    void testCriticalBloodPressureFromWebSocket() throws Exception {
        server = startServer("8,1000,SystolicBP,185.0");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        List<PatientRecord> records = storage.getRecords(8, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(185.0, records.get(0).getMeasurementValue(), 0.001);

        // alert generator should trigger a critical alert here without throwing
        assertDoesNotThrow(() -> alertGenerator.evaluateData(new Patient(8)));
    }

    // saturation below 92 should also make it through the pipeline cleanly
    @Test
    void testLowOxygenSaturationFromWebSocket() throws Exception {
        server = startServer("11,1000,Saturation,88.0");

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        List<PatientRecord> records = storage.getRecords(11, 0, Long.MAX_VALUE);
        assertEquals(1, records.size());
        assertEquals(88.0, records.get(0).getMeasurementValue(), 0.001);
        assertDoesNotThrow(() -> alertGenerator.evaluateData(new Patient(11)));
    }

    // four patients sending records in one stream — each must only see their own data
    @Test
    void testMultiplePatientsInOneStream() throws Exception {
        List<String> messages = new ArrayList<>();
        for (int p = 1; p <= 4; p++) {
            for (int i = 0; i < 5; i++) {
                messages.add(p + "," + (1000 + i * 500L) + ",HeartRate," + (60 + i));
            }
        }

        server = startServer(messages.toArray(new String[0]));
        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        for (int p = 1; p <= 4; p++) {
            assertEquals(5, storage.getRecords(p, 0, Long.MAX_VALUE).size(),
                    "Patient " + p + " should have exactly 5 records");
        }
    }

    // bad messages in the stream must not corrupt the good ones that come before or after
    @Test
    void testCorruptedMessagesDoNotAffectValidOnes() throws Exception {
        server = startServer(
                "20,1000,HeartRate,65.0",  // valid
                "NOT_VALID_AT_ALL",         // skipped
                "20,X,HeartRate,65.0",      // bad timestamp — skipped
                "20,3000,HeartRate,67.0"   // valid
        );

        DataStorage storage = DataStorage.getInstance();
        connect(storage);
        server.waitForClient();

        // only the two valid messages made it through
        assertEquals(2, storage.getRecords(20, 0, Long.MAX_VALUE).size());
        assertDoesNotThrow(() -> alertGenerator.evaluateData(new Patient(20)));
    }

    // hammers addPatientData from multiple threads simultaneously to confirm no data gets lost
    @Test
    void testConcurrentStorageUpdatesAreThreadSafe() throws Exception {
        int threadCount = 4;
        int recordsPerThread = 50;
        DataStorage storage = DataStorage.getInstance();

        // latch holds all threads at the starting line so they actually run in parallel
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            // each thread writes to a unique patient id to keep count checks simple
            final int patientId = 100 + t;
            new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < recordsPerThread; i++) {
                        storage.addPatientData(patientId, 60.0 + i, "HeartRate", 1000L + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        start.countDown();
        assertTrue(done.await(5, TimeUnit.SECONDS));

        // every single record must be accounted for — no silent drops under concurrent writes
        for (int t = 0; t < threadCount; t++) {
            assertEquals(recordsPerThread,
                    storage.getRecords(100 + t, 0, Long.MAX_VALUE).size(),
                    "Thread " + t + " should have written " + recordsPerThread + " records");
        }
    }

    // --- helpers ---

    private StubServer startServer(String... messages) throws InterruptedException {
        StubServer s = new StubServer(messages);
        s.startAndWait();
        return s;
    }

    private void connect(DataStorage storage) throws Exception {
        new WebSocketDataReader("ws://localhost:" + server.getPort()).readData(storage);
    }

    // -------------------------------------------------------------------------

    // simple websocket server that sends a fixed set of messages on connect and nothing else
    private static class StubServer extends WebSocketServer {

        private final String[] messages;
        // fires when the server socket is bound — used to avoid connecting too early
        private final CountDownLatch startLatch  = new CountDownLatch(1);
        // fires when a client has connected and messages have been sent
        private final CountDownLatch clientLatch = new CountDownLatch(1);

        StubServer(String... messages) {
            // port 0 so each test gets its own port — no leftover TIME_WAIT from previous tests
            super(new InetSocketAddress(0));
            this.messages = messages;
            setReuseAddr(true);
        }

        void startAndWait() throws InterruptedException {
            start();
            startLatch.await(5, TimeUnit.SECONDS);
        }

        void waitForClient() throws InterruptedException {
            clientLatch.await(5, TimeUnit.SECONDS);
            // give the reader's message callbacks a moment to finish
            Thread.sleep(300);
        }

        @Override
        public void onStart() {
            startLatch.countDown();
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            for (String msg : messages) conn.send(msg);
            clientLatch.countDown();
        }

        @Override public void onClose(WebSocket conn, int code, String reason, boolean remote) {}
        @Override public void onMessage(WebSocket conn, String message) {}
        @Override public void onError(WebSocket conn, Exception ex) { ex.printStackTrace(); }
    }
}
