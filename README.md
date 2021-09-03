# datahub-java-client-sdk
Client library in Java for talking to datahub and universal api spec endpoints

# Pre-Reqs
- sdkman
- java
- gradle

# To Build and Test
> ./gradlew build

# Usage

After building the project the artifact:

`datahubclientsdk.jar` 

should be included on the classpath.

Import 

`import mimiro.datahub.clientsdk.DatahubClient`

and then create a new datahub client:

```
    var apiEndpoint = "API_ENDPOINT";
    var authEndpoint = "AUTH_ENDPOINT";
    var clientId = "CLIENT_ID";
    var clientSecret = "CLIENT_SECRET";
    var audience = "AUDIENCE";
    var grantType = "GRANT_TYPE";

    var client = new DatahubClient(apiEndpoint,
                                    authEndpoint,
                                    clientId,
                                    clientSecret,
                                    audience,
                                    grantType);
```

The client can then be used to getEntities, getChanges and getDatasets.

```
    try {
        var datasetName = "DATASET_NAME";
        var entities = client.getEntities(datasetName, null);

        // code to work with the entities returned.

    } catch (ClientException e) {
        assertTrue("unexpected exception", true);
    }
```
