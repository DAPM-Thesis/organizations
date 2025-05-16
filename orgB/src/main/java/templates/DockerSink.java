package templates;

import communication.message.Message;
import communication.message.impl.event.Event;
import org.springframework.boot.autoconfigure.jms.JmsProperties;
import pipeline.processingelement.Configuration;
import pipeline.processingelement.Sink;
import repository.TemplateRepository;
import utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class DockerSink extends Sink {
    public DockerSink(Configuration configuration) {
        super(configuration);
        System.out.println("New docker image created.");
        System.out.println("\t\tWith image ID: " + configuration.get("imageID"));
    }

    @Override
    public boolean start() {
        super.start();
        System.out.println("Docker Sink started.");
        for (int i : getConsumers().keySet()) {
            System.out.println("\t\tWith processing element consumer port " + i
                    + " with brokerURL " + getConsumers().get(i).getBrokerUrl()
                    + " and topic " + getConsumers().get(i).getTopic());
        }
        return true;
    }

    @Override
    public boolean terminate() {
        super.terminate();
        System.out.println("Docker Sink terminated.");
        return true;
    }

    @Override
    public void observe(Pair<Message, Integer> pair) {

    }

    @Override
    protected Map<Class<? extends Message>, Integer> setConsumedInputs() {
        Map<Class<? extends Message>, Integer> map = new HashMap<>();
        map.put(Event.class, 1);
        return map;
    }
}
