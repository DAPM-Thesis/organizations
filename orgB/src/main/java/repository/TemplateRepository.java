package repository;

import org.springframework.stereotype.Repository;
import pipeline.processingelement.ProcessingElement;
import templates.EventOperatorB;
import templates.HeuristicsMiner;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Repository
public class TemplateRepository {

    private Map<String, Class<? extends ProcessingElement>> templates;

    public TemplateRepository() {
        templates = new HashMap<>();
        templates.put("SimpleOperator", EventOperatorB.class);
        templates.put("HeuristicsMiner", HeuristicsMiner.class);
    }

    public <T extends ProcessingElement> T createInstanceFromTemplate(String templateID) {
        Class<? extends ProcessingElement> template = templates.get(templateID);
        if(template == null) { throw new RuntimeException("Template " + templateID + " not found"); }
            try {
                return (T) template.getDeclaredConstructor().newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
    }
}