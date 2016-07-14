package services;

import com.fasterxml.jackson.databind.JsonNode;
import play.cache.CacheApi;

import javax.inject.Inject;

public class CacheService {
    private CacheApi cacheApi;

    @Inject
    public CacheService(CacheApi cacheApi) {
        this.cacheApi = cacheApi;
    }

    public JsonNode get(String key) {
        Object obj = cacheApi.get(key);

        return (obj instanceof JsonNode) ? ((JsonNode) obj).deepCopy() : null;
    }

    public void set(String key, JsonNode value, int lifeInSeconds) {
        cacheApi.set(key, value.deepCopy(), lifeInSeconds);
    }
}
