package mimiro.datahub.clientsdk;

import java.util.HashMap;
import java.util.Map;

public class Context {
    private final Map<String, String> namespacePrefixMappings;

    public Context() {
        this.namespacePrefixMappings = new HashMap<String, String>();
    }

    public void setNamespacePrefixExpansionMapping(String prefix, String expansion) {
        this.namespacePrefixMappings.put(prefix, expansion);
    }

    public String getExpansionForPrefix(String prefix) {
        if (this.namespacePrefixMappings.containsKey(prefix)){
            return this.namespacePrefixMappings.get(prefix);
        } else {
          throw new RuntimeException("no expansion for prefix " + prefix);
        }
    }
}
