package repository;

import org.springframework.stereotype.Repository;
import pipeline.processingelement.ProcessingElement;
import pipeline.processingelement.accesscontrolled.PEToken;
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

    // TODO: make it more generic later
    public <T extends ProcessingElement> T createInstanceFromTemplate(
            String templateID, PEToken token) {

        Class<? extends ProcessingElement> template = templates.get(templateID);
        if (template == null)
            throw new RuntimeException("No template found for template ID: " + templateID);

        try {
            return (T) template
                    .getDeclaredConstructor(PEToken.class)
                    .newInstance(token);
        } catch (InvocationTargetException | InstantiationException
                 | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}