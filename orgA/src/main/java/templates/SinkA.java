package templates;

import communication.message.Message;
import communication.message.impl.event.Attribute;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;

import java.util.HashMap;
import java.util.Map;

public class SinkA extends Sink {

    public SinkA(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void observe(Message message, int portNumber) {
//        if (message instanceof Event) {
//            Event e = (Event) message;
//            System.out.println("SinkA:- Received Event on port " + portNumber + ":");
//            System.out.println("  caseID   = " + e.getCaseID());
//            System.out.println("  activity = " + e.getActivity());
//            System.out.println("  timestamp= " + e.getTimestamp());
//            System.out.println("  attributes:");
//            for (Attribute<?> attr : e.getAttributes()) {
//                System.out.println("    • " + attr.getName() + " = " + attr.getValue());
//            }
//        } else {
//            System.out.println("SinkA:- Received non-Event message: " + message);
//        }
        Event e = (Event) message;
        System.out.println(this + " received: " + "  caseID:   " + e.getCaseID()+",  activity:   " + e.getActivity() +",  timestamp:" + e.getTimestamp()+ " on port " + portNumber);
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
