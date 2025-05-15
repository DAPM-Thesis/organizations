package templates;

import communication.message.Message;
import communication.message.impl.Metrics;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeanJoinSaver extends Sink {
    private final List<Metrics> recentMessages;
    private int firstCount = 0;
    private int secondCount = 0;
    private final int firstPort = 1;
    private final int secondPort = 2;

    public MeanJoinSaver(Configuration configuration) {
        super(configuration);
        recentMessages = new ArrayList<>(2);
        recentMessages.set(0, null);
        recentMessages.set(1, null);
    }

    @Override
    public void observe(Pair<Message, Integer> pair) {
        Metrics receivedMetrics = (Metrics) pair.first();
        int port = pair.second();

        if (port == firstPort) {
            firstCount++;
            handlePortMetrics(firstPort, secondPort, receivedMetrics, firstCount);
        } else if (port == secondPort) {
            secondCount++;
            handlePortMetrics(secondPort, firstPort, receivedMetrics, secondCount);
        } else {
            throw new IllegalStateException("Unexpected port number " + port + " in MeanJoinSaver");
        }

    }

    private void handlePortMetrics(int currentPort, int otherPort, Metrics receivedMetrics, int updatedCount) {
        if (recentMessages.get(currentPort) == null) {
            recentMessages.set(currentPort, receivedMetrics);
        } else if (recentMessages.get(otherPort) == null) {
            recentMessages.set(currentPort, updateMetricAverage(receivedMetrics, currentPort, updatedCount));
        } else {
            saveMetrics(recentMessages);
            reset();
        }
    }


    private Metrics updateMetricAverage(Metrics newMetrics, int port, int updatedCount) {
        List<Double> updated = new ArrayList<>();
        final int oldCount = updatedCount-1;
        List<Double> oldMetricsList = recentMessages.get(port).getMetrics();
        List<Double> newMetricsList = newMetrics.getMetrics();
        for (int i = 0; i < oldMetricsList.size(); i++) {
            double oldValue = oldMetricsList.get(i);
            double newValue = (oldValue*oldCount + newMetricsList.get(i)) / updatedCount;
            updated.add(newValue);
        }

        return new Metrics(updated);
    }

    private void saveMetrics(List<Metrics> recentMessages) {

    }

    private void reset() {
        recentMessages.set(firstPort, null);
        recentMessages.set(secondPort, null);
        firstCount = 0;
        secondCount = 0;
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        return Map.of(Metrics.class, 2);
    }
}
