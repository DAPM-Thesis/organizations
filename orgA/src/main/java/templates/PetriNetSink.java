package templates;

import communication.message.Message;
import communication.message.impl.petrinet.PetriNet;
import communication.message.impl.petrinet.Place;
import communication.message.impl.petrinet.Transition;
import communication.message.impl.petrinet.arc.Arc;

import communication.message.impl.petrinet.arc.PlaceToTransitionArc;
import communication.message.impl.petrinet.arc.TransitionToPlaceArc;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static guru.nidi.graphviz.engine.Format.SVG;
import static guru.nidi.graphviz.engine.Graphviz.fromGraph;
import static guru.nidi.graphviz.model.Factory.*;

public class PetriNetSink extends Sink {
    private static volatile boolean shutdownScheduled = false;
    private static final long START_TIME = System.currentTimeMillis();
    public PetriNetSink(Configuration configuration) {
        super(configuration);
        //scheduleAutoTerminate();
    }

    @Override
    public void observe(Pair<Message, Integer> messageAndPortNumber) {
        PetriNet petriNet = (PetriNet) messageAndPortNumber.first();
        MutableGraph dotGraph = constructDotGraph(petriNet);
        try {
            fromGraph(dotGraph)
                    .render(SVG)
                    .toFile(new File("orgA/src/main/resources/sinks/outputs/petriNet.svg"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PetriNet", e);
        }
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(PetriNet.class, 1);
        return map;
    }

    private MutableGraph constructDotGraph(PetriNet petriNet) {
        MutableGraph dotGraph = mutGraph("petriNet").setDirected(true);

        Set<Place> places = petriNet.getPlaces();
        Set<Transition> transitions = petriNet.getTransitions();
        Set<Arc> arcs = petriNet.getFlowRelation();

        Set<PlaceToTransitionArc> placeToTransitionArcs = arcs.stream()
                .filter(arc -> arc instanceof PlaceToTransitionArc)
                .map(arc -> (PlaceToTransitionArc) arc)
                .collect(Collectors.toSet());

        Set<TransitionToPlaceArc> transitionToPlaceArcs = arcs.stream()
                .filter(arc -> arc instanceof TransitionToPlaceArc)
                .map(arc -> (TransitionToPlaceArc) arc)
                .collect(Collectors.toSet());


        Map<String, MutableNode> nodeMap = new HashMap<>();

        for (Place p : places) {
            MutableNode placeNode = mutNode(p.getID()).add(Shape.CIRCLE, Color.GREEN);
            dotGraph.add(placeNode);
            nodeMap.put(p.getID(), placeNode);
        }

        for (Transition t : transitions) {
            MutableNode transNode = mutNode(t.getID()).add(Shape.BOX, Color.BLUE);
            dotGraph.add(transNode);
            nodeMap.put(t.getID(), transNode);
        }

        for (PlaceToTransitionArc a : placeToTransitionArcs) {
            String src = a.getSource().getID();
            String tgt = a.getTarget().getID();
            if(nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                nodeMap.get(src).addLink(nodeMap.get(tgt));
            }
        }

        for (TransitionToPlaceArc a : transitionToPlaceArcs) {
            String src = a.getSource().getID();
            String tgt = a.getTarget().getID();
            if(nodeMap.containsKey(src) && nodeMap.containsKey(tgt)) {
                nodeMap.get(src).addLink(nodeMap.get(tgt));
            }
        }
        return dotGraph;
    }

    private void scheduleAutoTerminate() {
        if (shutdownScheduled) {
            return;
        }
        shutdownScheduled = true;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PetriNetSink-AutoShutdown");
            t.setDaemon(true);
            return t;
        });

        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - START_TIME;
        long delayUntilThirtySeconds = 30_000 - elapsedSinceStart;
        if (delayUntilThirtySeconds < 0) {
            delayUntilThirtySeconds = 0;
        }

        scheduler.schedule(() -> {
            try {
                System.out.println("PETRINETSINK- 30 s passed—sleeping for another 30 s to trigger missed‐heartbeat…");
                Thread.sleep(30_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("60 s since start—calling terminate() on PetriNetSink.");
            boolean didTerminate = this.terminate();
            if (didTerminate) {
                System.out.println("PetriNetSink terminated successfully after hold.");
            } else {
                System.err.println("PetriNetSink failed to terminate.");
            }
        }, delayUntilThirtySeconds, TimeUnit.MILLISECONDS);
    }
}
