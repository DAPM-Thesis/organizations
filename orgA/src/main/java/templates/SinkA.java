package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SinkA extends Sink {
    private static final long START_TIME = System.currentTimeMillis();
    private static volatile boolean shutdownScheduled = false;
    public SinkA(Configuration configuration) {
        super(configuration);
        //scheduleAutoTerminate();
    }
    @Override
    public void observe(Pair<Message, Integer> messageAndPortNumber) {
        //System.out.println(this + " received: " + messageAndPortNumber.first() + " on port " + messageAndPortNumber.second());
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }

    private void scheduleAutoTerminate() {
        if (shutdownScheduled) {
            return;
        }
        shutdownScheduled = true;

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EventOperatorB-AutoShutdown");
            t.setDaemon(true);
            return t;
        });

        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - START_TIME;
        long delayUntilThirtySeconds = 30_000 - elapsedSinceStart;
        if (delayUntilThirtySeconds < 0) {
            // If more than 30 s have already passed, schedule immediately
            delayUntilThirtySeconds = 0;
        }

        scheduler.schedule(() -> {
            try {
                System.out.println("30 s passed—sleeping for another 30 s to trigger missed‐heartbeat…");
                Thread.sleep(30_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }

            System.out.println("60 s since start—calling terminate() on EventOperatorB.");
            boolean didTerminate = this.terminate();
            if (didTerminate) {
                System.out.println("EventOperatorB terminated successfully after hold.");
            } else {
                System.err.println("EventOperatorB failed to terminate.");
            }
        }, delayUntilThirtySeconds, TimeUnit.MILLISECONDS);
    }
}
