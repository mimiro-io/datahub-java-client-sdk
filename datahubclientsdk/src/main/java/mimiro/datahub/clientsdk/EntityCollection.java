package mimiro.datahub.clientsdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntityCollection {
    private Context context;
    private List<Entity> entities;
    private String continuationToken;

    public EntityCollection() {
        entities = new ArrayList<Entity>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public List<Entity> getEntities() {
        return this.entities;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String getContinuationToken() {
        return this.continuationToken;
    }
}
