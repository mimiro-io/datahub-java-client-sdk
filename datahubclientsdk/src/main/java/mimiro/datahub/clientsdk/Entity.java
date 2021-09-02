package mimiro.datahub.clientsdk;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    final private String id;
    private boolean isDeleted;
    private Map<String, Object> properties;
    private Map<String, String> references;

    public Entity(String id) {
        this.id = id;
        this.properties = new HashMap<String, Object>();
        this.references = new HashMap<String, String>();
    }

}
