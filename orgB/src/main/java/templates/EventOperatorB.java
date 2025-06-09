package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.SimpleOperator;

import java.util.HashMap;
import java.util.Map;

public class EventOperatorB extends SimpleOperator<Event> {

    public EventOperatorB(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    @Override
    protected Event process(Message message, int portNumber) {
        System.out.println("Event Arrived!!!");
        return (Event) message;
    }
}