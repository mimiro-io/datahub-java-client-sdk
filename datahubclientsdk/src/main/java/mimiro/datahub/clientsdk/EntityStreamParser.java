package mimiro.datahub.clientsdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EntityStreamParser {

    public EntityCollection parseData(InputStream data) {
        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonFactory jsonFactory = objectMapper.getFactory();
        final JsonParser jsonParser;
        final EntityCollection entityCollection = new EntityCollection();

        try {
            jsonParser = jsonFactory.createParser(data);
        } catch (final IOException e) {
            throw new RuntimeException("There was a problem setting up the JsonParser: " + e.getMessage(), e);
        }

        try {
            // Check that the first element is the start of an array
            final JsonToken arrayStartToken = jsonParser.nextToken();
            if (arrayStartToken != JsonToken.START_ARRAY) {
                throw new IllegalStateException("The first element of the Json structure was expected to be a start array token, but it was: " + arrayStartToken);
            }

            boolean moreToRead = true;
            Map<String, Object> objectMap;
            while (moreToRead) {
                final JsonToken nextToken = jsonParser.nextToken();

                // Check for the end of the array which will mean we're done
                if (nextToken == JsonToken.END_ARRAY) {
                    moreToRead = false;
                    continue;
                }

                // Make sure the next token is the start of an object
                if (nextToken != JsonToken.START_OBJECT) {
                    throw new IllegalStateException("The next token of Json structure was expected to be a start object token, but it was: " + nextToken);
                }

                // Get the next product and make sure it's not null
                objectMap = jsonParser.readValueAs(new TypeReference<Map<String, Object>>() { });
                if (objectMap == null) {
                    throw new IllegalStateException("The next parsed object of the Json structure was null");
                }

                String id = getId(objectMap);
                if (Objects.equals(id, "@context")) {
                    Context context = makeContextFromMap(objectMap);
                    entityCollection.setContext(context);
                } else if (Objects.equals(id, "@continuation")) {
                    String token = getContinuationTokenFromMap(objectMap);
                    entityCollection.setContinuationToken(token);
                } else {
                    Entity entity = makeEntityFromMap(objectMap);
                    entityCollection.addEntity(entity);
                }
            }

        } catch (final Exception e) {
            throw new RuntimeException("There was a problem initializing the first element of the Json Structure: " + e.getMessage(), e);
        }

        return entityCollection;
    }

    private String getId(Map<String, Object> entity) {
        if (entity.containsKey("id")) {
            return entity.get("id").toString();
        } else {
            return null;
        }
    }

    private String getContinuationTokenFromMap(Map<String, Object> objectMap) {
        return objectMap.get("token").toString();
    }

    private Entity makeEntityFromMap(Map<String, Object> objectMap){
        String id = getId(objectMap);
        Entity entity = new Entity(id);

        // iterate props
        var props = (Map<String, Object>) objectMap.get("props");
        if (props != null) {
            for (Map.Entry<String,Object> entry : props.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    var containedMap = (Map<String,Object>) value;
                    var containedEntity = makeEntityFromMap(containedMap);
                    entity.setProperty(key, containedEntity);
                } else if (value instanceof List) {
                    var listValues = (List<Object>) value;
                    var newValues = new ArrayList<Object>();
                    for (Object o : listValues) {
                        if (o instanceof Map) {
                            var containedMap = (Map<String,Object>) value;
                            var containedEntity = makeEntityFromMap(containedMap);
                            newValues.add(entity);
                        } else {
                            newValues.add(o);
                        }
                    }
                    entity.setProperty(key, newValues);
                } else {
                    entity.setProperty(key, value);
                }
            }
        }

        // iterate refs
        var refs = (Map<String, Object>) objectMap.get("refs");
        if (refs != null) {
            for (Map.Entry<String,Object> entry : refs.entrySet()) {
                String key = entry.getKey();
                Object references = entry.getValue();
                if (references instanceof List) {
                    var listrefs = (List<String>) references;
                    entity.setReferences(key, listrefs);
                } else if (references instanceof String) {
                    String ref = (String) references;
                    entity.setReference(key, ref);
                }
            }
        }

        return entity;
    }

    private Context makeContextFromMap(Map<String, Object> objectMap) {
        Context context = new Context();
        Map<String,Object> mappings = (Map<String, Object>) objectMap.get("namespaces");
        for (Map.Entry<String,Object> entry : mappings.entrySet()) {
            String prefix = entry.getKey();
            String expansion = entry.getValue().toString();
            context.setNamespacePrefixExpansionMapping(prefix, expansion);
        }

        return context;
    }
}
