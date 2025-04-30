package templates;

import communication.message.impl.event.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pipeline.processingelement.Source;
import pipeline.processingelement.accesscontrolled.PEToken;

import java.util.HashSet;
import java.util.Random;

@Component
public class SourceA extends Source<Event> {

    private final String[] activities = { "Act1", "Act2", "Act3" };
    private final Random   rand       = new Random();

    @Autowired
    public SourceA(PEToken token) {
        super(token);
    }

    @Override
    public Event process() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
