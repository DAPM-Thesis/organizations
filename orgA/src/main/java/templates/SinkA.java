package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class SinkA extends Sink {

    public SinkA(Configuration configuration) {
        super(configuration);
    }

    @Override
    public void observe(Pair<Message, Integer> messageAndPortNumber) {
        System.out.println("SinkA!!");
        Event e = (Event) messageAndPortNumber.first();
        System.out.println(this + " received: " + "  caseID:   " + e.getCaseID()+",  activity:   "
                + e.getActivity() +",  timestamp:" + e.getTimestamp()+ " on port " + messageAndPortNumber.second());
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
