# Cardio Data Simulator

The Cardio Data Simulator is a Java-based application designed to simulate real-time cardiovascular data for multiple patients. This tool is particularly useful for educational purposes, enabling students to interact with real-time data streams of ECG, blood pressure, blood saturation, and other cardiovascular signals.

## Features

- Simulate real-time ECG, blood pressure, blood saturation, and blood levels data.
- Supports multiple output strategies:
  - Console output for direct observation.
  - File output for data persistence.
  - WebSocket and TCP output for networked data streaming.
- Configurable patient count and data generation rate.
- Randomized patient ID assignment for simulated data diversity.

## Getting Started

### Prerequisites

- Java JDK 11 or newer.
- Maven for managing dependencies and compiling the application.

### Installation

1. Clone the repository:

   ```sh
   git clone https://github.com/tpepels/signal_project.git
   ```

2. Navigate to the project directory:

   ```sh
   cd signal_project
   ```

3. Compile and package the application using Maven:
   ```sh
   mvn clean package
   ```
   This step compiles the source code and packages the application into an executable JAR file located in the `target/` directory.

### Running the Simulator

After packaging, you can run the simulator directly from the executable JAR:

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar
```

To run with specific options (e.g., to set the patient count and choose an output strategy):

```sh
java -jar target/cardio_generator-1.0-SNAPSHOT.jar --patient-count 100 --output file:./output
```

### Supported Output Options

- `console`: Directly prints the simulated data to the console.
- `file:<directory>`: Saves the simulated data to files within the specified directory.
- `websocket:<port>`: Streams the simulated data to WebSocket clients connected to the specified port.
- `tcp:<port>`: Streams the simulated data to TCP clients connected to the specified port.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
## UML Diagrams

### Diagram 1 — Alert Generation System (`uml_models/alert_generation_system.png`)

This diagram models how the system detects and dispatches clinical alerts. `AlertGenerator` is the central coordinator: it receives `PatientData`, checks it against one or more `Threshold` objects, and creates an `Alert` when a limit is breached. The `Alert` is then handed off to `AlertManager`, which holds a queue and is responsible for dispatching alerts onward (e.g. to a nurse station). The `Patient` class anchors everything to a real person — `PatientData` records belong to a patient, and alerts reference that same patient by ID. The key design choice here is keeping threshold logic in its own class so new thresholds (e.g. for a new vital sign) can be added without touching `AlertGenerator`.



### Diagram 2 — Data Storage and Retrieval (`uml_models/data_storage_and_retrieval.png`)

This diagram shows how patient records are stored and accessed. `DataStorage` is the central repository: it holds a map of patient IDs to lists of `PatientData` records and exposes time-range queries and version lookups. `DataRetriever` wraps storage and provides higher-level queries (history by time range, latest record). `AccessController` enforces who is allowed to read which records — it validates user IDs and logs every access attempt. The `*` multiplicity on `PatientData → DataStorage` reflects that one storage instance holds records for many patients.



### Diagram 3 — Patient Identity Management (`uml_models/patient_identity_management.png`)

This diagram captures how incoming data is matched to a known hospital patient. `PatientIdentifier` holds a `Map<Integer, HospitalPatient>` and is responsible for looking up the right patient record when new `PatientData` arrives. `IdentityManager` sits on top and orchestrates the full verification flow — it calls `PatientIdentifier` to match data, checks integrity, and logs anomalies. `HospitalPatient` is a plain data class representing a patient record in the hospital system.



### Diagram 4 — Data Listener and Ingestion (`uml_models/data_listener_ingestion.png`)

This diagram models how raw data arrives from different sources and gets parsed into a common format before reaching storage. `DataListener` is an abstract class that defines the contract for any data source — subclasses (`TCPDataListener`, `WebSocketDataListener`, `FileDataListener`) each implement `startListening()` differently but share the `onDataReceived` and `disconnect` logic. When raw data arrives it is passed to `DataParser`, which detects the format and converts it into `PatientData`. `DataSourceAdapter` bridges the listener and the parser: it receives raw strings, triggers parsing, and forwards the result to `DataStorage`. This design makes it easy to add a new source (e.g. Bluetooth) by subclassing `DataListener` without touching the parsing or storage layers.

---

## Log of tests summary Part 5
- Attached at the end in Tests Part 5 LOG folder

## Project Members
- Student ID: i6427181
- Student ID: i6416851
