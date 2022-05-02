package mimiro.datahub.clientsdk;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DatahubClient {

    private String clientSecret;
    private String clientId;
    private String audience;
    private String grantType;
    private String authEndpoint;
    private String apiEndpoint;
    private boolean noAuthentication;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public DatahubClient() {
        noAuthentication = true;
    }

    public DatahubClient(String apiEndpoint, String authEndpoint, String clientId, String clientSecret, String audience, String grantType) {
        this.apiEndpoint = apiEndpoint;
        this.authEndpoint = authEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.audience = audience;
        this.grantType = grantType;
    }

    private String makeTokenRequestPayload() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode user = mapper.createObjectNode();
        user.put("client_id", clientId);
        user.put("client_secret", clientSecret);
        user.put("audience", audience);
        user.put("grant_type", grantType);
        return user.toString();
    }

    private String getToken() throws ClientException {
        if (noAuthentication) {
            return null;
        }

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(makeTokenRequestPayload()))
                .uri(URI.create(authEndpoint))
                .setHeader("User-Agent", "Datahub Java Client SDK")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var json = response.body();
            ObjectMapper mapper = new ObjectMapper();
            var data = mapper.readTree(json);
            var token = data.get("access_token");
            return token.asText();
        } catch (IOException | InterruptedException e) {
            throw new ClientException(e.getMessage());
        }
    }

    private HttpRequest buildGetRequest(String url) throws ClientException {
        if (noAuthentication) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("User-Agent", "Datahub Java Client SDK")
                    .build();
            return request;
        } else {
            String token = getToken();
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url))
                    .setHeader("Authorization", "Bearer " + token)
                    .build();
            return request;
        }
    }

    public List<Dataset> getDatasets() throws ClientException {
        var datasetsUrl = apiEndpoint + "/datasets";
        var request = buildGetRequest(datasetsUrl);

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            var jsonData = response.body();
            return makeDatasetsFromJson(jsonData);
        } catch (IOException | InterruptedException e) {
            throw new ClientException(e.getMessage());
        }
    }

    private List<Dataset> makeDatasetsFromJson(String data) {
        InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
        ObjectMapper mapper = new ObjectMapper();
        var datasets = new ArrayList<Dataset>();

        try {
            JsonParser parser = mapper.getFactory().createParser(stream);

            if(parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected an array");
            }
            while(parser.nextToken() == JsonToken.START_OBJECT) {
                ObjectNode node = mapper.readTree(parser);
                var name = node.get("Name").asText();
                var dataset = new Dataset(name);
                datasets.add(dataset);
            }
            parser.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return datasets;
    }

    public EntityCollection getEntities(String datasetName, String continuationToken, String limit) throws ClientException{
        EntityStreamParser esp = new EntityStreamParser();

        var entitiesUrl = apiEndpoint + "/datasets/" + datasetName + "/entities";
        if (continuationToken != null || limit != null) {
            String suffix = "";
            entitiesUrl += "?";

            if (continuationToken != null) {
                try {
                    entitiesUrl += "from=" + URLEncoder.encode(continuationToken, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ClientException(e.getMessage());
                }
                suffix = "&";
            }
            if (limit != null) {
                entitiesUrl += suffix + "limit=" + limit;
            }

        }

        var request = buildGetRequest(entitiesUrl);

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            var jsonStream = response.body();
            return esp.parseData(jsonStream);
        } catch (IOException | InterruptedException e) {
            throw new ClientException(e.getMessage());
        }
    }

    public EntityCollection getChanges(String datasetName, String continuationToken, String limit) throws ClientException{
        EntityStreamParser esp = new EntityStreamParser();

        var changesUrl = apiEndpoint + "/datasets/" + datasetName + "/changes";
        if (continuationToken != null || limit != null) {
            String suffix = "";
            changesUrl += "?";

            if (continuationToken != null) {
                try {
                    changesUrl += "since=" + URLEncoder.encode(continuationToken, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new ClientException(e.getMessage());
                }
                suffix = "&";
            }
            if (limit != null) {
                changesUrl += suffix + "limit=" + limit;
            }
        }

        var request = buildGetRequest(changesUrl);

        try {
            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            var jsonStream = response.body();
            return esp.parseData(jsonStream);
        } catch (IOException | InterruptedException e) {
            throw new ClientException(e.getMessage());
        }
    }
}
