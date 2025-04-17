package repository;

import pipeline.processingelement.InstanceMetaData;
import pipeline.processingelement.ProcessingElement;

import java.util.*;

public class PEInstanceRepository {

    public Map<String, InstanceMetaData> instanceMetaData;
    public Map<String, ProcessingElement> instances;

    public PEInstanceRepository() {
        instanceMetaData = new HashMap<>();
        instances = new HashMap<>();
    }

    public String storeInstanceMetaData(String templateID, int instanceNumber, String brokerURL, String topic, boolean isProducer) {
        String instanceMetaDataID = "instance-metadata" + UUID.randomUUID();
        instanceMetaData.put(instanceMetaDataID, new InstanceMetaData(
                instanceMetaDataID,
                templateID,
                instanceNumber,
                brokerURL,
                topic,
                isProducer,
                null));
        return instanceMetaDataID;
    }

    public InstanceMetaData getInstanceMetaData(String instanceMetaDataID) {
        return instanceMetaData.get(instanceMetaDataID);
    }

    public String storeInstance(ProcessingElement instance, String[] instanceMetaDataIDS) {
        String instanceID = "instance-" + UUID.randomUUID();
        instances.put(instanceID, instance);
        for (String instanceMetaData : instanceMetaDataIDS) {
            InstanceMetaData metadata = this.instanceMetaData.get(instanceMetaData);
            if (metadata != null && metadata.instanceID() == null) {
                InstanceMetaData updated = new InstanceMetaData(
                        metadata.instanceDetailID(),
                        metadata.templateID(),
                        metadata.instanceNumber(),
                        metadata.brokerURL(),
                        metadata.topic(),
                        metadata.isProducer(),
                        instanceID
                );
                this.instanceMetaData.put(instanceMetaData, updated);
            }
        }
        return instanceID;
    }

    public <T extends ProcessingElement> T getInstance(String instanceID) {
        return (T) instances.get(instanceID);
    }
}
