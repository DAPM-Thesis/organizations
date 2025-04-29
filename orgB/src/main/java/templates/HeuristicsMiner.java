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
            String jarPath = "templates/algorithms/JarFile-1.0-SNAPSHOT-jar-with-dependencies.jar";
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

            // Wait for the process to finish
            int exitCode = process.waitFor();
            System.out.println("JAR process exited with code: " + exitCode);

            String jarOutputString = outputBuilder.toString();
            System.out.println("JAR Output: " + jarOutputString);

            PetriNet petriNet = (PetriNet) MessageFactory.deserialize(jarOutputString);
            Boolean success = true;

            return new Pair<>(petriNet, success);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean publishCondition(Pair<PetriNet, Boolean> petriNetBooleanPair) {
        return true;
    }
}
