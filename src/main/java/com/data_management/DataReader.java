package com.data_management;

import java.io.IOException;

// defines how any data source plugs into the system
// file readers and websocket readers both implement this
public interface DataReader {

    // one-shot read — works well for files where all data is already there
    // streaming readers should use connect() instead, but this stays for compatibility
    void readData(DataStorage dataStorage) throws IOException;

    // open a live connection and keep feeding data into storage until disconnect() is called
    // default just calls readData() so file-based readers don't have to change anything
    default void connect(DataStorage dataStorage) throws IOException {
        readData(dataStorage);
    }

    // cleanly close whatever connection was opened by connect()
    // default does nothing — only streaming readers need to override this
    default void disconnect() {}
}
