package com.cardio_generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.cardio_generator.generators.AlertStateGenerator;

import com.cardio_generator.generators.BloodPressureDataGenerator;
import com.cardio_generator.generators.BloodSaturationDataGenerator;
import com.cardio_generator.generators.BloodLevelsDataGenerator;
import com.cardio_generator.generators.ECGDataGenerator;
import com.cardio_generator.outputs.ConsoleOutputStrategy;
import com.cardio_generator.outputs.FileOutputStrategy;
import com.cardio_generator.outputs.OutputStrategy;
import com.cardio_generator.outputs.TcpOutputStrategy;
import com.cardio_generator.outputs.WebSocketOutputStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This class simulates medical data for a configurable number of patients, generating different types
 * of health readings on a schedule and sending them to a chosen output.
 *
 * Implemented as a Singleton so only one simulator instance exists.
 *
 * @author isaac
 */
public class HealthDataSimulator {


    private static HealthDataSimulator instance;

    private int patientCount = 50; // Default number of patients
    private ScheduledExecutorService scheduler;
    private OutputStrategy outputStrategy = new ConsoleOutputStrategy(); // Default output strategy
    private final Random random = new Random();

    /**
     * Private constructor prevents external instantiation
     */
    private HealthDataSimulator() {
    }

    /**
     * Provides global access to the single simulator instance
     *
     * @return the singleton instance of HealthDataSimulator
     */
    public static synchronized HealthDataSimulator getInstance() {
        if (instance == null) {
            instance = new HealthDataSimulator();
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {

        HealthDataSimulator simulator = HealthDataSimulator.getInstance();

        simulator.start(args);
    }

    /**
     * Starts the simulator after parsing arguments and initializing tasks
     *
     * @param args command-line arguments
     * @throws IOException if output setup fails
     */
    public void start(String[] args) throws IOException {

        parseArguments(args);

        scheduler = Executors.newScheduledThreadPool(patientCount * 4);

        List<Integer> patientIds = initializePatientIds(patientCount);
        Collections.shuffle(patientIds); // Randomize the order of patient IDs

        scheduleTasksForPatients(patientIds);
    }

    /**
     * Parses command line arguments for the user to set, such as patient count, and how
     * it's outputted
     *
     * @param args parses the command-line arguments provided to the application and configures the
     *             simulation accordingly. Handles patient count, output strategy, and help options.
     * @throws IOException if an output directory cannot be created when file output is selected
     */
    private void parseArguments(String[] args) throws IOException {

        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {

                case "-h":
                    printHelp();
                    System.exit(0);
                    break;

                case "--patient-count":

                    if (i + 1 < args.length) {

                        try {

                            patientCount = Integer.parseInt(args[++i]);

                        } catch (NumberFormatException e) {

                            System.err.println(
                                    "Error: Invalid number of patients. Using default value: "
                                            + patientCount);
                        }
                    }

                    break;

                case "--output":

                    if (i + 1 < args.length) {

                        String outputArg = args[++i];

                        if (outputArg.equals("console")) {

                            outputStrategy = new ConsoleOutputStrategy();

                        } else if (outputArg.startsWith("file:")) {

                            String baseDirectory = outputArg.substring(5);

                            Path outputPath = Paths.get(baseDirectory);

                            if (!Files.exists(outputPath)) {
                                Files.createDirectories(outputPath);
                            }

                            outputStrategy = new FileOutputStrategy(baseDirectory);

                        } else if (outputArg.startsWith("websocket:")) {

                            try {

                                int port = Integer.parseInt(outputArg.substring(10));

                                // Initialize your WebSocket output strategy here
                                outputStrategy = new WebSocketOutputStrategy(port);

                                System.out.println("WebSocket output will be on port: " + port);

                            } catch (NumberFormatException e) {

                                System.err.println(
                                        "Invalid port for WebSocket output. Please specify a valid port number.");
                            }

                        } else if (outputArg.startsWith("tcp:")) {

                            try {

                                int port = Integer.parseInt(outputArg.substring(4));

                                // Initialize your TCP socket output strategy here
                                outputStrategy = new TcpOutputStrategy(port);

                                System.out.println("TCP socket output will be on port: " + port);

                            } catch (NumberFormatException e) {

                                System.err.println(
                                        "Invalid port for TCP output. Please specify a valid port number.");
                            }

                        } else {

                            System.err.println("Unknown output type. Using default (console).");
                        }
                    }

                    break;

                default:

                    System.err.println("Unknown option '" + args[i] + "'");
                    printHelp();
                    System.exit(1);
            }
        }
    }

    /**
     * Prints application help information
     */
    private void printHelp() {

        System.out.println("Usage: java HealthDataSimulator [options]");

        System.out.println("Options:");

        System.out.println("  -h                       Show help and exit.");

        System.out.println(
                "  --patient-count <count>  Specify the number of patients to simulate data for (default: 50).");

        System.out.println("  --output <type>          Define the output method. Options are:");

        System.out.println("                             'console' for console output,");

        System.out.println("                             'file:<directory>' for file output,");

        System.out.println("                             'websocket:<port>' for WebSocket output,");

        System.out.println("                             'tcp:<port>' for TCP socket output.");

        System.out.println("Example:");

        System.out.println(
                "  java HealthDataSimulator --patient-count 100 --output websocket:8080");

        System.out.println(
                "  This command simulates data for 100 patients and sends the output to WebSocket clients connected to port 8080.");
    }

    /**
     * Initializes sequential patient IDs
     *
     * @param patientCount number of patients
     * @return list of patient IDs
     */
    private List<Integer> initializePatientIds(int patientCount) {

        List<Integer> patientIds = new ArrayList<>();

        for (int i = 1; i <= patientCount; i++) {
            patientIds.add(i);
        }

        return patientIds;
    }

    /**
     * Schedules all data generation tasks for every patient
     *
     * @param patientIds list of patient IDs
     */
    private void scheduleTasksForPatients(List<Integer> patientIds) {

        ECGDataGenerator ecgDataGenerator = new ECGDataGenerator(patientCount);

        BloodSaturationDataGenerator bloodSaturationDataGenerator =
                new BloodSaturationDataGenerator(patientCount);

        BloodPressureDataGenerator bloodPressureDataGenerator =
                new BloodPressureDataGenerator(patientCount);

        BloodLevelsDataGenerator bloodLevelsDataGenerator =
                new BloodLevelsDataGenerator(patientCount);

        AlertStateGenerator alertStateGenerator =
                new AlertStateGenerator(patientCount);

        for (int patientId : patientIds) {

            scheduleTask(
                    () -> ecgDataGenerator.generate(patientId, outputStrategy),
                    1,
                    TimeUnit.SECONDS);

            scheduleTask(
                    () -> bloodSaturationDataGenerator.generate(patientId, outputStrategy),
                    1,
                    TimeUnit.SECONDS);

            scheduleTask(
                    () -> bloodPressureDataGenerator.generate(patientId, outputStrategy),
                    1,
                    TimeUnit.MINUTES);

            scheduleTask(
                    () -> bloodLevelsDataGenerator.generate(patientId, outputStrategy),
                    2,
                    TimeUnit.MINUTES);

            scheduleTask(
                    () -> alertStateGenerator.generate(patientId, outputStrategy),
                    20,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Schedules a repeating task with a small random startup delay
     *
     * @param task the task to execute
     * @param period repeat period
     * @param timeUnit unit of time
     */
    private void scheduleTask(Runnable task, long period, TimeUnit timeUnit) {

        scheduler.scheduleAtFixedRate(
                task,
                random.nextInt(5),
                period,
                timeUnit);
    }
}