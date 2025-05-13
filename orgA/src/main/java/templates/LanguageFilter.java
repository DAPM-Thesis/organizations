package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import communication.message.serialization.MessageSerializer;
import communication.message.serialization.deserialization.MessageFactory;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.operator.SimpleOperator;

import java.util.HashMap;
import java.util.Map;

public class LanguageFilter extends SimpleOperator<Event> {
    private final String language;

    public LanguageFilter(Configuration configuration) {
        super(configuration);
        language = configuration.get("string").toString().toLowerCase();
    }

    @Override
    protected Event process(Message message, int portNumber) {
        MessageSerializer serializer = new MessageSerializer();
        message.acceptVisitor(serializer);
        String event = serializer.getSerialization();
        if (event.contains("\"domain\": \"" + language + ".")) {
            System.out.println("Event: " + event);
            Message msg = MessageFactory.deserialize(event);
            return (Event) msg;
        }
        return null;
    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
