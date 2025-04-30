package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.accesscontrolled.PEToken;
import pipeline.processingelement.operator.SimpleOperator;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventOperatorB extends SimpleOperator<Event> {

    @Autowired
    public EventOperatorB(PEToken token) {
        super(token);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    @Override
    protected Event process(Message message, int portNumber) {
        System.out.println(getClass().getSimpleName() +
                " applied on " + message + " from port " + portNumber);
        return (Event) message;
    }
}
