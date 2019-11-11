package cern.c2mon.server.elasticsearch.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.*;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class EmbeddedElastic {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient httpClient = new HttpClient();

    public List<String> fetchAllDocuments() {
        return new ArrayList<>();
    }

    public void deleteIndex(String indexName) {
        if (indexExists(indexName)) {
            HttpDelete request = new HttpDelete(url("/" + indexName));
            httpClient.execute(request, (Consumer<CloseableHttpResponse>) response -> assertOk(response, "Delete request resulted in error"));
            waitForClusterYellow();
        } else {
            // logger.warn("Index: {} does not exists so cannot be removed", indexName);
        }
    }

    private boolean indexExists(String indexName) {
        HttpHead request = new HttpHead(url("/" + indexName));
        return httpClient.execute(request, response -> response.getStatusLine().getStatusCode() == 200);
    }

    public void refreshIndices() {
        HttpPost request = new HttpPost(url("/_refresh"));
        try {
            httpClient.execute(request);
        } finally {
            request.releaseConnection();
        }
    }

    public List<String> fetchAllDocuments(String... indices) {
        return fetchAllDocuments(null, indices);
    }

    private List<String> fetchAllDocuments(String routing, String... indices) {
        if (indices.length == 0) {
            return searchForDocuments(Optional.empty()).collect(Collectors.toList());
        } else {
            return Stream.of(indices)
                    .flatMap((index) -> searchForDocuments(Optional.of(index), Optional.ofNullable(routing)))
                    .collect(Collectors.toList());
        }
    }

    private Stream<String> searchForDocuments(Optional<String> indexMaybe) {
        return searchForDocuments(indexMaybe, Optional.empty());
    }

    private Stream<String> searchForDocuments(Optional<String> indexMaybe, Optional<String> routing) {
        String searchCommand = prepareQuery(indexMaybe, routing);
        String body = fetchDocuments(searchCommand);
        return parseDocuments(body);
    }

    private String prepareQuery(Optional<String> indexMaybe, Optional<String> routing) {

        String routingQueryParam = routing
                .map(r -> "?routing=" + r)
                .orElse("");

        return indexMaybe
                .map(index -> "/" + index + "/_search" + routingQueryParam)
                .orElse("/_search");
    }

    private String fetchDocuments(String searchCommand) {
        HttpGet request = new HttpGet(url(searchCommand));
        return httpClient.execute(request, response -> {
            assertOk(response, "Error during search (" + searchCommand + ")");
            return readBodySafely(response);
        });
    }

    private Stream<String> parseDocuments(String body) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.readTree(body);
            return StreamSupport.stream(jsonNode.get("hits").get("hits").spliterator(), false)
                    .map(hitNode -> hitNode.get("_source"))
                    .map(JsonNode::toString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitForClusterYellow() {
        HttpGet request = new HttpGet(url("/_cluster/health?wait_for_status=yellow&timeout=60s"));
        httpClient.execute(request, (Consumer<CloseableHttpResponse>) response -> assertOk(response, "Cluster does not reached yellow status in specified timeout"));
    }

    private String url(String path) {
        return "http://localhost:9200" + path;
    }

    private void assertOk(CloseableHttpResponse response, String message) {
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IllegalStateException(message + "\nResponse body:\n" + readBodySafely(response));
        }
    }

    private String readBodySafely(CloseableHttpResponse response) {
        try {
            return response.getEntity().getContent().toString();
        } catch (IOException e) {
//            logger.error("Error during reading response body", e);
            return "";
        }
    }
}
