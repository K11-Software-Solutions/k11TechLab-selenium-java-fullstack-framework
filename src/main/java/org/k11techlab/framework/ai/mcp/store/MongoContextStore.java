package org.k11techlab.framework.ai.mcp.store;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MongoContextStore implements ContextStore {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;
    private final boolean connected;

    public MongoContextStore(String uri, String dbName, String collectionName) {
        MongoClient tempClient = null;
        MongoCollection<Document> tempCollection = null;
        boolean isConnected = false;
        
        try {
            // Configure MongoDB client with short timeouts
            com.mongodb.MongoClientSettings settings = com.mongodb.MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString(uri))
                    .applyToSocketSettings(builder -> 
                        builder.connectTimeout(2, TimeUnit.SECONDS)
                               .readTimeout(2, TimeUnit.SECONDS))
                    .applyToClusterSettings(builder -> 
                        builder.serverSelectionTimeout(2, TimeUnit.SECONDS))
                    .build();
                    
            tempClient = MongoClients.create(settings);
            // Test connection with ping
            tempClient.getDatabase(dbName).runCommand(new Document("ping", 1));
            tempCollection = tempClient.getDatabase(dbName).getCollection(collectionName);
            isConnected = true;
            System.out.println("✅ Successfully connected to MongoDB at " + uri);
        } catch (Exception e) {
            System.err.println("⚠️  Failed to connect to MongoDB at " + uri + ": " + e.getMessage());
            System.err.println("   MongoDB features will be unavailable. Using in-memory store instead.");
            if (tempClient != null) {
                try {
                    tempClient.close();
                } catch (Exception closeEx) {
                    // Safe to ignore: MongoDB client cleanup failure when connection never succeeded
                    // The client is already in a failed state and will be discarded
                }
                tempClient = null;
            }
        }
        
        this.mongoClient = tempClient;
        this.collection = tempCollection;
        this.connected = isConnected;
    }
    
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void put(String key, String value) {
        if (!connected) {
            System.err.println("⚠️  MongoDB not connected, cannot store key: " + key);
            return;
        }
        try {
            Document doc = new Document("key", key).append("value", value);
            collection.deleteMany(Filters.eq("key", key));
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("⚠️  Error storing to MongoDB: " + e.getMessage());
        }
    }

    @Override
    public String get(String key) {
        if (!connected) {
            System.err.println("⚠️  MongoDB not connected, cannot retrieve key: " + key);
            return "";
        }
        try {
            Document doc = collection.find(Filters.eq("key", key)).first();
            return doc != null ? doc.getString("value") : "";
        } catch (Exception e) {
            System.err.println("⚠️  Error reading from MongoDB: " + e.getMessage());
            return "";
        }
    }

    @Override
    public List<Document> listAll() {
        if (!connected) {
            System.err.println("⚠️  MongoDB not connected, returning empty list");
            return new ArrayList<>();
        }
        try {
            List<Document> all = new ArrayList<>();
            for (Document doc : collection.find()) all.add(doc);
            return all;
        } catch (Exception e) {
            System.err.println("⚠️  Error listing from MongoDB: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
            } catch (Exception e) {
                System.err.println("⚠️  Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}
