package com.example.orgb.service;

import communication.message.impl.event.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In‐process Heuristic Miner: counts direct‐follows per case,
 * and computes the dependency measure:
 *
 *   dep(A→B) = (count(A→B) - count(B→A)) / (count(A→B) + count(B→A) + 1)
 */
public class SimpleHeuristicMiner {

    // directFollowCounts[A].get(B) == # of times we saw A→B
    private final Map<String,Map<String,Long>> directFollowCounts = new ConcurrentHashMap<>();

    // lastActivityPerCase[caseId] = name of the last activity seen
    private final Map<String,String> lastActivityPerCase = new ConcurrentHashMap<>();

    /** Call this for each incoming event */
    public void addEvent(Event e) {
        String caseId  = e.getCaseID();
        String activity= e.getActivity();

        String prev = lastActivityPerCase.get(caseId);
        if (prev != null) {
            directFollowCounts
                    .computeIfAbsent(prev, k -> new ConcurrentHashMap<>())
                    .merge(activity, 1L, Long::sum);
        }
        lastActivityPerCase.put(caseId, activity);
    }

    /** Compute all current direct‐follow metrics */
    public List<DirectFollow> getMetrics() {
        List<DirectFollow> out = new ArrayList<>();
        for (var fromEntry : directFollowCounts.entrySet()) {
            String from = fromEntry.getKey();
            for (var toEntry : fromEntry.getValue().entrySet()) {
                String to     = toEntry.getKey();
                long   freqAB = toEntry.getValue();
                long   freqBA = directFollowCounts
                        .getOrDefault(to, Collections.emptyMap())
                        .getOrDefault(from, 0L);
                double dep = (freqAB - freqBA) / (double)(freqAB + freqBA + 1);
                out.add(new DirectFollow(from, to, freqAB, dep));
            }
        }
        return out;
    }

    /** One arc’s metrics */
    public static class DirectFollow {
        public final String from, to;
        public final long   frequency;
        public final double dependency;
        public DirectFollow(String from, String to, long freq, double dep) {
            this.from       = from;
            this.to         = to;
            this.frequency  = freq;
            this.dependency = dep;
        }
        @Override public String toString() {
            return String.format("%s→%s (freq=%d, dep=%.3f)",
                    from, to, frequency, dependency);
        }
    }
}

