package templates;

import com.example.orgb.service.HeuristicMinerClient;
import com.example.orgb.service.SimpleHeuristicMiner;
import com.example.orgb.service.SimpleHeuristicMiner.DirectFollow;
import communication.message.Message;
import communication.message.impl.event.*;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.SimpleOperator;

import java.util.*;

public class EventOperatorB extends SimpleOperator<Event> {

//    private final HeuristicMinerClient miner;
private final SimpleHeuristicMiner miner = new SimpleHeuristicMiner();
    public EventOperatorB(Configuration configuration) {
        super(configuration);
//        String jarPath = "orgB/src/main/java/templates/algorithm/heuristics-miner.jar";
//        this.miner = new HeuristicMinerClient(jarPath);
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
        Event e = (Event) message;
        // 1) Update the in‐process miner
        miner.addEvent(e);

        // 2) Fetch metrics
        List<DirectFollow> metrics = miner.getMetrics();

        // 3) Print + attach
        System.out.println("Heuristic‐miner metrics:");
        Map<String,Attribute<?>> miningResultMap = new LinkedHashMap<>();
        for (DirectFollow m : metrics) {
            //System.out.println("  → " + df);
            System.out.println("  → " + m);
            System.out.println("hm_from:" + m.from);
            System.out.println("hm_to-" + m.to);
            System.out.println("frequency-" + m.frequency);
            System.out.println("dependency-" + m.dependency);

            miningResultMap.put("hm_from",
                    new Attribute<>("hm_from", m.from));
            miningResultMap.put("hm_to",
                    new Attribute<>("hm_to", m.to));
            miningResultMap.put("frequency",
                    new Attribute<>("frequency", String.valueOf(m.frequency)));
            miningResultMap.put("dependency",
                    new Attribute<>("dependency",  String.valueOf(m.dependency)));
        }
        e.getAttributes().add(
                new Attribute<>("miningresult", null, miningResultMap)
        );
        return e;
    }
    @Override
    public boolean terminate() {
        return super.terminate();
    }
}