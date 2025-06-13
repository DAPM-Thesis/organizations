package templates;

import com.example.orgb.service.SimpleHeuristicMiner;
import com.example.orgb.service.SimpleHeuristicMiner.DirectFollow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import communication.message.Message;
import communication.message.impl.event.*;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.SimpleOperator;

import java.time.Instant;
import java.util.*;

public class EventOperatorB extends SimpleOperator<Event> {

private final SimpleHeuristicMiner miner = new SimpleHeuristicMiner();
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
        Event e = (Event) message;
        // 1) Update the in‐process miner
        miner.addEvent(e);

        // 2) Fetch metrics
        List<DirectFollow> metrics = miner.getMetrics();
        long timestamp = Instant.parse(e.getTimestamp()).toEpochMilli();
        // 3) Print + attach
        System.out.println("Heuristic‐miner metrics:");
        List<Map<String, Object>> arcList = new ArrayList<>();
        for (DirectFollow m : metrics) {
            System.out.println("  → " + m);
            System.out.println("hm_from:" + m.from);
            System.out.println("hm_to-" + m.to);
            System.out.println("frequency-" + m.frequency);
            System.out.println("dependency-" + m.dependency);
            Map<String, Object> arc = new HashMap<>();
            arc.put("arc_from", m.from);
            arc.put("arc_to", m.to);
            arc.put("frequency", m.frequency);
            arc.put("dependency", m.dependency);
            arc.put("timestamp", timestamp);
            arcList.add(arc);
        }
        String payload;
        try {
            payload = new ObjectMapper().writeValueAsString(arcList);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println("Payload:" + payload);
        Set<Attribute<?>> newAttributes = new HashSet<>(e.getAttributes());
        Attribute<String> miningMetrics = new Attribute<>("mining_metrics", payload);
        newAttributes.add(miningMetrics);
        Event newEvent = new Event(
                e.getCaseID(),
                e.getActivity(),
                e.getTimestamp(),
                newAttributes
        );
        return newEvent;
    }
    @Override
    public boolean terminate() {
        return super.terminate();
    }
}