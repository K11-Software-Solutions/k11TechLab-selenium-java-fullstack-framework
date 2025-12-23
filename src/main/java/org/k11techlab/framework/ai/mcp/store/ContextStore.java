package org.k11techlab.framework.ai.mcp.store;

import org.bson.Document;
import java.util.List;

/**
 * Interface for storing and retrieving context data.
 * Implementations can use different backends (MongoDB, in-memory, etc.)
 */
public interface ContextStore {
    
    /**
     * Store a key-value pair
     * @param key The key to store
     * @param value The value to store
     */
    void put(String key, String value);
    
    /**
     * Retrieve a value by key
     * @param key The key to retrieve
     * @return The value, or empty string if not found
     */
    String get(String key);
    
    /**
     * List all stored documents
     * @return List of all documents
     */
    List<Document> listAll();
    
    /**
     * Close any resources used by this store
     */
    default void close() {
        // Default no-op implementation
    }
}
