package repository;

import org.springframework.stereotype.Repository;
import pipeline.processingelement.ProcessingElement;
import utils.IDGenerator;

import java.util.*;

@Repository
public class PEInstanceRepository {
    public Map<String, ProcessingElement> instances;
    public PEInstanceRepository() {
        instances = new HashMap<>();
    }
    public void storeInstance(String instanceID, ProcessingElement instance) {
        instances.put(instanceID, instance);
        System.out.printf("[PE-REGISTER] template=%s  class=%s  instanceId=%s%n",
                instance.getClass().getSimpleName(),
                instance.getClass().getName(),
                instanceID);
    }
    public <T extends ProcessingElement> T getInstance(String instanceID) {
        return (T) instances.get(instanceID);
    }
}
