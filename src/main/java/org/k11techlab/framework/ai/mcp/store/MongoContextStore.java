package org.k11techlab.framework.ai.mcp.store;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoContextStore {
    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public MongoContextStore(String uri, String dbName, String collectionName) {
        this.mongoClient = MongoClients.create(uri);
        this.collection = mongoClient.getDatabase(dbName).getCollection(collectionName);
    }

    public void put(String key, String value) {
        Document doc = new Document("key", key).append("value", value);
        collection.deleteMany(Filters.eq("key", key));
        collection.insertOne(doc);
    }

    public String get(String key) {
        Document doc = collection.find(Filters.eq("key", key)).first();
        return doc != null ? doc.getString("value") : "";
    }

    public List<Document> listAll() {
        List<Document> all = new ArrayList<>();
        for (Document doc : collection.find()) all.add(doc);
        return all;
    }
}
