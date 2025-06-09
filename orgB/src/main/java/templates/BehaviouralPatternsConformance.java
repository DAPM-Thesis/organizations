package templates;


import communication.message.Message;
import communication.message.impl.Metrics;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.SimpleOperator;
import utils.JsonUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BehaviouralPatternsConformance extends SimpleOperator<Metrics> {

    private final Object processLock = new Object();

    private Process process;
    private BufferedWriter jarInput;
    private BufferedReader jarOutput;

    public BehaviouralPatternsConformance(Configuration configuration) {
        super(configuration);
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
                    String jarPath = "orgB/src/main/java/templates/libs/behavioural-patterns-conformance.jar";
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
    protected Metrics process(Message message, int portNumber) {
        synchronized (processLock) {
            try {
                startProcess();
                String event = JsonUtil.toJson(message);

                jarInput.write(event);
                jarInput.newLine();
                jarInput.flush();

                // Collect output from the JAR with timeout
                long startTime = System.currentTimeMillis();
                List<String> scores = new ArrayList<>(3);
                String line;
                while (scores.size() < 3 && (line = jarOutput.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        scores.add(line.trim());
                    }
                    if (System.currentTimeMillis() - startTime > 10000) {
                        throw new IOException("Timeout while reading behavioural-patterns conformance scores from JAR");
                    }
                }
                String conformance = scores.get(0);
                String completeness = scores.get(1);
                String confidence = scores.get(2);
                return new Metrics(Double.parseDouble(conformance), Double.parseDouble(completeness), Double.parseDouble(confidence));

            } catch (IOException e) {
                throw new RuntimeException("Error during processing in BehaviouralConformance", e);
            }
        }
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

}
