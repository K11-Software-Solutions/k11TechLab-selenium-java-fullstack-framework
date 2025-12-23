package org.k11techlab.framework.ai.mcp.store;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of ContextStore.
 * Used as a fallback when MongoDB is not available.
 */
public class InMemoryContextStore implements ContextStore {
    
    private final Map<String, String> store = new ConcurrentHashMap<>();
    
    @Override
    public void put(String key, String value) {
        store.put(key, value);
    }
    
    @Override
    public String get(String key) {
        return store.getOrDefault(key, "");
    }
    
    @Override
    public List<Document> listAll() {
        List<Document> documents = new ArrayList<>();
        for (Map.Entry<String, String> entry : store.entrySet()) {
            Document doc = new Document("key", entry.getKey())
                             .append("value", entry.getValue());
            documents.add(doc);
        }
        return documents;
    }
    
    @Override
    public void close() {
        // In-memory store doesn't require cleanup
    }
}
