package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GitHubApiService {
    private static final String BASE_URL = "https://api.github.com";
    private static final Integer CACHE_LIFE_IN_SECONDS = 60;

    private CacheService cacheService;
    private WSClient wsClient;

    @Inject
    public GitHubApiService(CacheService cacheService, WSClient wsClient) {
        this.cacheService = cacheService;
        this.wsClient = wsClient;
    }

    public boolean isHashExpired(String hash) {
        String decodedHash = new String(Base64.getUrlDecoder().decode(hash));

        return (new Date()).getTime() - new Long(decodedHash.split("\\|")[0]) > CACHE_LIFE_IN_SECONDS * 1000;
    }

    public CompletionStage<JsonNode> passthrough(String path) {
        WSRequest request = wsClient.url(BASE_URL + "/" + path);

        return doGet(request, getCacheKey(request, "", ""));
    }

    public CompletionStage<JsonNode> getTopReposByForks(
            Integer count,
            String url,
            String hash,
            Integer page
    ) {
        final String finalHash = (null == hash || hash.isEmpty()) ? generateHash() : hash;

        return getNetflixRepos(finalHash).thenApply(repos -> {
            if (!repos.isArray()) {
                return new ArrayNode(JsonNodeFactory.instance);
            }

            return getTopSubset((ArrayNode) repos, "forks_count", Long.class, count);
        }).thenApply(topRepos -> getPage(url, finalHash, page, 10, topRepos));
    }

    public CompletionStage<JsonNode> getTopReposByLastUpdated(
            Integer count,
            String url,
            String hash,
            Integer page
    ) {
        final String finalHash = (null == hash || hash.isEmpty()) ? generateHash() : hash;

        return getNetflixRepos(finalHash).thenApply(repos -> {
            if (!repos.isArray()) {
                return new ArrayNode(JsonNodeFactory.instance);
            }

            return getTopSubset((ArrayNode) repos, "updated_at", Date.class, count);
        }).thenApply(topRepos -> getPage(url, finalHash, page, 10, topRepos));
    }

    public CompletionStage<JsonNode> getTopReposByOpenIssues(
            Integer count,
            String url,
            String hash,
            Integer page
    ) {
        final String finalHash = (null == hash || hash.isEmpty()) ? generateHash() : hash;

        return getNetflixRepos(finalHash).thenApply(repos -> {
            if (!repos.isArray()) {
                return new ArrayNode(JsonNodeFactory.instance);
            }

            return getTopSubset((ArrayNode) repos, "open_issues", Long.class, count);
        }).thenApply(topRepos -> getPage(url, finalHash, page, 10, topRepos));
    }

    public CompletionStage<JsonNode> getTopReposByStars(
            Integer count,
            String url,
            String hash,
            Integer page
    ) {
        final String finalHash = (null == hash || hash.isEmpty()) ? generateHash() : hash;

        return getNetflixRepos(finalHash).thenApply(repos -> {
            if (!repos.isArray()) {
                return new ArrayNode(JsonNodeFactory.instance);
            }

            return getTopSubset((ArrayNode) repos, "stargazers_count", Long.class, count);
        }).thenApply(topRepos -> getPage(url, finalHash, page, 10, topRepos));
    }

    public CompletionStage<JsonNode> getTopReposByWatchers(
            Integer count,
            String url,
            String hash,
            Integer page
    ) {
        final String finalHash = (null == hash || hash.isEmpty()) ? generateHash() : hash;

        return getNetflixRepos(finalHash).thenApply(repos -> {
            if (!repos.isArray()) {
                return new ArrayNode(JsonNodeFactory.instance);
            }

            return getTopSubset((ArrayNode) repos, "watchers", Long.class, count);
        }).thenApply(topRepos -> getPage(url, finalHash, page, 10, topRepos));
    }

    private JsonNode getPage(String url, String hash, Integer page, Integer size, JsonNode items) {
        int end = page * size;
        int start = end - size;

        ObjectNode linksNode = new ObjectNode(JsonNodeFactory.instance);
        linksNode.set("first", new TextNode(url + "?hash=" + hash + "&page=1"));
        if (end < items.size()) {
            linksNode.set("next", new TextNode(url + "?hash=" + hash + "&page=" + (page + 1)));
        }
        if (start > 0) {
            linksNode.set("previous", new TextNode(url + "?hash=" + hash + "&page=" + (page - 1)));
        }

        ArrayNode pageItems = new ArrayNode(JsonNodeFactory.instance);
        while (start < end && start < items.size()) {
            pageItems.add(items.get(start++));
        }

        ObjectNode embeddedNode = new ObjectNode(JsonNodeFactory.instance);
        embeddedNode.set("items", pageItems);

        ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
        objectNode.set("page", new IntNode(page));
        objectNode.set("size", new IntNode(size));
        objectNode.set("total", new IntNode(items.size()));
        objectNode.set("_links", linksNode);
        objectNode.set("_embedded", embeddedNode);

        return objectNode;
    }

    private <T extends Comparable<T>> T transform(Class<T> fieldType, String str) {
        if (Date.class.isAssignableFrom(fieldType)) {
            try {
                return fieldType.cast((new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")).parse(str));
            } catch (ParseException e) {
                return null;
            }
        } else if (Long.class.isAssignableFrom(fieldType)) {
            return fieldType.cast(new Long(str));
        } else {
            return null;
        }
    }

    private <T extends Comparable<T>> ArrayNode getTopSubset(ArrayNode repos, String fieldName, Class<T> fieldType, int count) {
        ArrayNode topRepos = new ArrayNode(JsonNodeFactory.instance);

        if (null == repos) {
            return topRepos;
        }

        if (repos.size() <= count) {
            return repos;
        }

        for (int i = 0; i < count; i++) {
            T curMax = null;
            JsonNode curMaxNode = null;
            int currentMaxIndex = 0;
            for (int j = 0; j < repos.size(); j++) {
                JsonNode repo = repos.get(j);
                T curValue = transform(fieldType, repo.get(fieldName).asText());

                if (null == curValue) {
                    continue;
                }

                if (curMax == null) {
                    curMax = curValue;
                    curMaxNode = repo;
                    currentMaxIndex = j;
                }

                if (curValue.compareTo(curMax) == 1) {
                    curMax = curValue;
                    curMaxNode = repo;
                    currentMaxIndex = j;
                }
            }
            topRepos.add(curMaxNode);
            repos.remove(currentMaxIndex);
        }

        return topRepos;
    }

    private CompletionStage<JsonNode> getNetflixRepos(String hash) {
        WSRequest request = wsClient.url(BASE_URL + "/orgs/Netflix/repos");

        return doGet(request, getCacheKey(request, "", hash));
    }

    private String generateHash() {
        return Base64.getUrlEncoder().encodeToString(((new Date()).getTime() + "|" + UUID.randomUUID().toString()).getBytes());
    }

    private CompletionStage<JsonNode> doGet(WSRequest request, final String key) {
        JsonNode cachedResponse = cacheService.get(key);
        if (null != cachedResponse) {
            return CompletableFuture.supplyAsync(() -> cachedResponse);
        }

        return request.get().thenApply(
                WSResponse::asJson
        ).thenApply(jsonNode -> {
            if (jsonNode != null) {
                cacheService.set(key, jsonNode, CACHE_LIFE_IN_SECONDS);
            }

            return jsonNode;
        });
    }

    private String getCacheKey(WSRequest request, String body, String hash) {
        String key = request.getUrl() + hash + body;
        for (Map.Entry<String, Collection<String>> entry : request.getHeaders().entrySet()) {
            key += entry.getKey();
            String delim = "=";
            for (String headerValue : entry.getValue()) {
                key += delim + headerValue;
                delim = ",";
            }
        }
        for (Map.Entry<String, Collection<String>> entry : request.getQueryParameters().entrySet()) {
            key += entry.getKey();
            String delim = "=";
            for (String headerValue : entry.getValue()) {
                key += delim + headerValue;
                delim = ",";
            }
        }

        return key;
    }
}
