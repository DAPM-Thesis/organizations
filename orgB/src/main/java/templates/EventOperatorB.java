package templates;

import algorithm.Algorithm;
import communication.message.Message;
import communication.message.impl.event.Event;
import pipeline.processingelement.operator.SimpleOperator;

import java.util.HashMap;
import java.util.Map;

public class EventOperatorB extends SimpleOperator<Event> {

    public EventOperatorB(Algorithm<Message, Event> algorithm) {
        super(algorithm);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

}
