package mimiro.datahub.clientsdk;

import java.util.ArrayList;
import java.util.List;

public class DatahubClient {

    public interface EntityHandler {
        void ProcessEntity(Entity entity);
    }

    public interface ContextHandler {
        void ProcessContext(Context context);
    }

    public List<Dataset> getDatasets() {
        return new ArrayList<Dataset>();
    }

    public String getEntities(String datasetName, String continuationToken, EntityHandler entityHandler, ContextHandler contextHandler){
        return "";
    }

}
