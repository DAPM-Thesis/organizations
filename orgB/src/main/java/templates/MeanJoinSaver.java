package templates;

import communication.message.Message;
import communication.message.impl.Metrics;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeanJoinSaver extends Sink {
    private final List<Metrics> recentMessages;
    private int firstCount = 0;
    private int secondCount = 0;
    private final int firstPort = 0;
    private final int secondPort = 1;
    private int count = 0;

    private double firstAvgConformance = 0.0;
    private double secondAvgConformance = 0.0;
    private double firstAvgCompleteness = 0.0;
    private double secondAvgCompleteness = 0.0;
    private double firstAvgConfidence = 0.0;
    private double secondAvgConfidence = 0.0;


    public MeanJoinSaver(Configuration configuration) {
        super(configuration);
        recentMessages = new ArrayList<>(2);
        recentMessages.add(null);
        recentMessages.add(null);
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
        System.out.println("Saving metrics: " + recentMessages.get(0) + " and " + recentMessages.get(1));
        double newFirstConformance = recentMessages.get(firstPort).getMetrics().getFirst();
        double newFirstCompleteness = recentMessages.get(firstPort).getMetrics().get(1);
        double newFirstConfidence = recentMessages.get(firstPort).getMetrics().getLast();

        double newSecondConformance = recentMessages.get(secondPort).getMetrics().getFirst();
        double newSecondCompleteness = recentMessages.get(secondPort).getMetrics().get(1);
        double newSecondConfidence = recentMessages.get(secondPort).getMetrics().getLast();

        count++;
        firstAvgConformance = firstAvgConformance + (newFirstConformance - firstAvgConformance) / count;
        firstAvgCompleteness = firstAvgCompleteness + (newFirstCompleteness - firstAvgCompleteness) / count;
        firstAvgConfidence = firstAvgConfidence + (newFirstConfidence - firstAvgConfidence) / count;

        secondAvgConformance = secondAvgConformance + (newSecondConformance - secondAvgConformance) / count;
        secondAvgCompleteness = secondAvgCompleteness + (newSecondCompleteness - secondAvgCompleteness) / count;
        secondAvgConfidence = secondAvgConfidence +(newSecondConfidence - secondAvgConfidence) / count;

        String toSave = firstAvgConformance + "\n" + secondAvgConformance +
                "\n" + firstAvgCompleteness + "\n" + secondAvgCompleteness +
                "\n" + firstAvgConfidence + "\n" + secondAvgConfidence;

        try {
            FileWriter fw = new FileWriter("orgB/src/main/resources/sinks/outputs/scores.txt", false);
            fw.write(toSave);
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
