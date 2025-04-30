package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.impl.petrinet.PetriNet;
import communication.message.serialization.MessageSerializer;
import communication.message.serialization.deserialization.MessageFactory;
import pipeline.processingelement.accesscontrolled.PEToken;
import pipeline.processingelement.operator.MiningOperator;
import utils.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HeuristicsMiner extends MiningOperator<PetriNet> {

    private final BufferedWriter jarInput;
    private final BufferedReader jarOutput;

    public HeuristicsMiner(PEToken token) {
        super(token);
        try {
            String jarPath = "orgB/src/main/java/templates/algorithms/heuristics-miner.jar";
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            jarInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            jarOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    @Override
    protected Pair<PetriNet, Boolean> process(Message message, int portNumber) {
        try {
            // Send input to the JAR
            MessageSerializer serializer = new MessageSerializer();
            message.acceptVisitor(serializer);
            String event = serializer.getSerialization();
            jarInput.write(event);
            jarInput.newLine();
            jarInput.flush();

            // Collect output from the JAR
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = jarOutput.readLine()) != null) {
                if (line.trim().isEmpty()) break;
                outputBuilder.append(line).append("\n");
            }

            String jarOutputString = outputBuilder.toString();
            PetriNet petriNet = (PetriNet) MessageFactory.deserialize(jarOutputString);
            Boolean publish = true;

            return new Pair<>(petriNet, publish);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean publishCondition(Pair<PetriNet, Boolean> petriNetBooleanPair) {
        return true;
    }
}
