package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.serialization.MessageSerializer;
import communication.message.serialization.deserialization.MessageFactory;
import pipeline.processingelement.operator.MiningOperator;
import utils.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HeuristicsMiner extends MiningOperator<PetriNet> {

    private final Object processLock = new Object();

    private Process process;
    private BufferedWriter jarInput;
    private BufferedReader jarOutput;

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

    public void close() {
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
    }
}
