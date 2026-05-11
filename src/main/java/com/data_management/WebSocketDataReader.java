package com.data_management;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// connects to a websocket server and streams incoming patient data into storage
// messages must follow the format: patientId,timestamp,label,value
public class WebSocketDataReader implements DataReader {

    private final URI serverUri;

    // kept as a field so disconnect() can close it later
    private WebSocketClient client;

    public WebSocketDataReader(String serverUri) throws URISyntaxException {
        this.serverUri = new URI(serverUri);
    }

    // bridges the old readData interface to the newer connect() approach
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        connect(dataStorage);
    }

    // builds the websocket client, wires up all the callbacks, then blocks until the handshake finishes
    @Override
    public void connect(DataStorage dataStorage) throws IOException {
        client = new WebSocketClient(serverUri) {

            // fires once the connection is established — just a confirmation log
            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connected to " + serverUri);
            }

            // every incoming message goes through parseAndStore
            // the dataStorage reference is captured here from the outer method parameter
            @Override
            public void onMessage(String message) {
                parseAndStore(message, dataStorage);
            }

            // server closed the connection — log it and move on, no crash
            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed (code=" + code
                        + (reason != null && !reason.isEmpty() ? ", reason=" + reason : "") + ")");
            }

            // something went wrong on the socket level — print it but don't rethrow
            @Override
            public void onError(Exception ex) {
                System.err.println("WebSocket error: " + ex.getMessage());
            }
        };

        try {
            // blocks until the handshake completes so the caller knows the connection is live
            client.connectBlocking();
        } catch (InterruptedException e) {
            // restore the interrupted flag and wrap so callers get a normal IOException
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while connecting to " + serverUri, e);
        }
    }

    // shuts down the connection gracefully — waits for any in-flight frames to finish
    @Override
    public void disconnect() {
        if (client != null) {
            try {
                client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // null it out so a second disconnect() call is a no-op
                client = null;
            }
        }
    }

    // parses a single csv-style message and writes the record into storage
    // bad messages are logged and skipped — they never reach storage
    private void parseAndStore(String message, DataStorage dataStorage) {
        String[] parts = message.split(",");

        // need exactly four fields: id, timestamp, label, value
        if (parts.length < 4) {
            System.err.println("Skipping malformed message: " + message);
            return;
        }

        try {
            // trim() handles any stray whitespace the server might send
            int patientId  = Integer.parseInt(parts[0].trim());
            long timestamp = Long.parseLong(parts[1].trim());
            String label   = parts[2].trim();
            double value   = Double.parseDouble(parts[3].trim());

            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            // one of the numeric fields was garbage — skip this message
            System.err.println("Could not parse message fields: " + message + " — " + e.getMessage());
        }
    }
}
