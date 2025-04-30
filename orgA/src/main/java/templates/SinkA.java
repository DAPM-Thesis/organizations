package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.Sink;
import pipeline.processingelement.accesscontrolled.PEToken;

import java.util.HashMap;
import java.util.Map;

@Component
public class SinkA extends Sink {
    @Autowired
    public SinkA(PEToken token) {
        super(token);
    }
    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
    
    @Override
    protected void handle(Message message, int portNumber) {
        System.out.println(this + " received: " + message + " on port " + portNumber);
    }
}
