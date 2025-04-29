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

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    @Override
    protected Pair<PetriNet, Boolean> process(Message message, int portNumber) {
        try {
            String jarPath = "orgB/src/main/java/templates/algorithms/JarFile-1.0-SNAPSHOT-jar-with-dependencies.jar";
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", jarPath);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedWriter jarInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            BufferedReader jarOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // Send input to the JAR
            MessageSerializer serializer = new MessageSerializer();
            message.acceptVisitor(serializer);
            String event = serializer.getSerialization();
            jarInput.write(event + "\n");
            jarInput.flush();
            jarInput.close();

            // Collect output from the JAR
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = jarOutput.readLine()) != null) {
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
