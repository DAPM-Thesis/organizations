package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.serialization.MessageSerializer;
import communication.message.serialization.deserialization.MessageFactory;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.MiningOperator;
import utils.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeuristicsMiner extends MiningOperator<PetriNet> {

    private final Object processLock = new Object();

    private Process process;
    private BufferedWriter jarInput;
    private BufferedReader jarOutput;

    private static final long START_TIME = System.currentTimeMillis();
    private static volatile boolean shutdownScheduled = false;
    public HeuristicsMiner(Configuration configuration) {
        super(configuration);
        //scheduleAutoTerminate();
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    private void startProcess() {
        synchronized (processLock) {
            try {
                if (process == null || !process.isAlive()) {
                    String jarPath = "orgB/src/main/java/templates/algorithm/heuristics-miner.jar";
                    ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
                    processBuilder.redirectErrorStream(true);
                    process = processBuilder.start();

                    jarInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    jarOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to start JAR process", e);
            }
        }
    }


    @Override
    protected Pair<PetriNet, Boolean> process(Message message, int portNumber) {
        synchronized (processLock) {
            try {
                startProcess();
                // Send input to the JAR
                MessageSerializer serializer = new MessageSerializer();
                message.acceptVisitor(serializer);
                String event = serializer.getSerialization();

                jarInput.write(event);
                jarInput.newLine();
                jarInput.flush();

                // Collect output from the JAR with timeout
                long startTime = System.currentTimeMillis();
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = jarOutput.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        break;
                    }
                    stringBuilder.append(line).append(System.lineSeparator());
                    if (System.currentTimeMillis() - startTime > 10000) {
                        throw new IOException("Timeout while reading response from JAR");
                    }
                }

                String statusLine = jarOutput.readLine();
                String content = stringBuilder.toString().trim();
                boolean isSuccess = Boolean.parseBoolean(statusLine);

                if (isSuccess) {
                    PetriNet petriNet = (PetriNet) MessageFactory.deserialize(content);
                    return new Pair<>(petriNet, isSuccess);
                }
                return new Pair<>(null, isSuccess);
            } catch (Exception e) {
                throw new RuntimeException("Error during processing in HeuristicsMiner", e);
            }
        }
    }


    @Override
    protected boolean publishCondition(Pair<PetriNet, Boolean> petriNetBooleanPair) {
        return petriNetBooleanPair.second();
    }

    @Override
    public boolean terminate() {
        super.terminate();
        synchronized (processLock) {
            try {
                if (jarInput != null) {
                    jarInput.close();
                }
                if (jarOutput != null) {
                    jarOutput.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to close JAR process", e);
            } finally {
                jarInput = null;
                jarOutput = null;
                process = null;
            }
        }
        return true;
    }
    private void scheduleAutoTerminate() {
        // Only schedule once per JVM (shared across all HeuristicsMiner instances)
        if (shutdownScheduled) {
            return;
        }
        shutdownScheduled = true;

        // Use a single-thread ScheduledExecutorService
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "HeuristicMiner-AutoShutdown");
            t.setDaemon(true);
            return t;
        });

        // Compute “delay until 30s after START_TIME”
        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - START_TIME;
        long delayUntilThirtySec = 30_000 - elapsedSinceStart;
        if (delayUntilThirtySec < 0) {
            delayUntilThirtySec = 0; // if more than 30 s have already passed, run immediately
        }

        // Step 1: After (delayUntilThirtySec) ms, run a task that sleeps 30 s then calls terminate()
        scheduler.schedule(() -> {
            try {
                System.out.println("30 s passed—sleeping for 30 s to trigger missed‐heartbeat…");
                Thread.sleep(30_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }

            // Step 2: Now that we slept an additional 30 s, ask to terminate this operator.
            // Since terminate() is non‐static, it will invoke the instance‐method on “this”—
            // but note: if multiple HeuristicsMiner instances exist, each one will also see
            // shutdownScheduled=true, so only the first caller’s scheduler thread gets created.
            //
            // In many pipeline frameworks, terminate() often signals to kill the whole pipeline.
            // We just invoke terminate() on the first‐constructed instance.
            System.out.println("60 s since start—calling terminate() on HeuristicsMiner.");
            boolean didTerminate = this.terminate();
            if (didTerminate) {
                System.out.println("HeuristicsMiner terminated successfully after hold.");
            } else {
                System.err.println("HeuristicsMiner failed to terminate.");
            }
        }, delayUntilThirtySec, TimeUnit.MILLISECONDS);
    }
}
