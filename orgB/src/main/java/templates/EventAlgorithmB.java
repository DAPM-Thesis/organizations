package templates;

import algorithm.Algorithm;
import communication.message.Message;
import communication.message.impl.event.Event;

public class EventAlgorithmB implements Algorithm<Message, Event> {

    @Override
    public Event run(Message event) {
        System.out.println(this.getClass().getSimpleName() + " applied.");
        return (Event) event;
    }
}