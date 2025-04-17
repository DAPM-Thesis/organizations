package repository;

import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.operator.Operator;
import templates.EventAlgorithmB;
import templates.EventOperatorB;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class TemplateRepository {

    private Map<String, Class<? extends ProcessingElement>> templates;

    public TemplateRepository() {
        templates = new HashMap<>();
        templates.put("SimpleOperator", EventOperatorB.class);
    }

    // TODO: make it more generic later
    public <T extends ProcessingElement> T createInstanceFromTemplate(String templateID) {
        Class<? extends ProcessingElement> template = templates.get(templateID);
        if(template != null) {
            try {
                EventAlgorithmB algorithm = new EventAlgorithmB();
                return (T) template.getDeclaredConstructor(EventAlgorithmB.class).newInstance(algorithm);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}