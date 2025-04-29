package templates;

import communication.message.Message;
import communication.message.impl.petrinet.PetriNet;
import pipeline.processingelement.Sink;

import java.util.HashMap;
import java.util.Map;

public class PetriNetSink extends Sink {
    @Override
    public void observe(Message message, int portNumber) {
        System.out.println(this + " received: " + message + " on port " + portNumber);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(PetriNet.class, 1);
        return map;
    }
}
