package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Sends patient health data over TCP to connected clients.
 * Starts a TCP server on the specified port and sends measurements
 * as comma-separated values to any connected client.
 */
public class TcpOutputStrategy implements OutputStrategy {

    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;

    /**
     * Creates a TCP server on the given port and waits for a client connection.
     * Accepts the first client connection in a separate thread to avoid blocking.
     *
     * @param port the port number to listen on
     */
    public TcpOutputStrategy(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("TCP Server started on port " + port);

            // Accept clients in a new thread to not block the main thread
            Executors.newSingleThreadExecutor().submit(() -> {
                try {
                    clientSocket = serverSocket.accept();
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    System.out.println("Client connected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a patient measurement to the connected TCP client.
     * Data is formatted as CSV: patientId,timestamp,label,data
     * Does nothing if no client is connected yet.
     *
     * @param patientId the patient's ID
     * @param timestamp when the measurement was taken
     * @param label the type of measurement
     * @param data the measurement value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        if (out != null) {
            String message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
            out.println(message);
        }
    }
}
