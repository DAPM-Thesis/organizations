package templates;

import communication.message.impl.event.Event;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.source.SimpleSource;

import java.util.HashSet;
import java.util.Random;

public class SourceA extends SimpleSource<Event> {

    private final String[] activities = {"Act1", "Act2", "Act3"};
    private final Random rand = new Random();

    public SourceA(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected Event process() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new Event(
                "CaseID" + rand.nextInt(0, 5),
                activities[rand.nextInt(activities.length)],
                "timestamp",
                new HashSet<>()
        );
    }
}
