package mimiro.datahub.clientsdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {
    final private String id;
    private boolean isDeleted;
    private final Map<String, Object> properties;
    private final Map<String, Object> references;

    public Entity(String id) {
        this.id = id;
        properties = new HashMap<>();
        references = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setProperty(String propertyType, Object value){
        properties.put(propertyType, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setReferences(String propertyType, List<String> references) {
        this.references.put(propertyType, references);
    }

    public void setReference(String propertyType, String reference) {
        this.references.put(propertyType, reference);
    }

    public Map<String, Object> getReferences() {
        return references;
    }

}
