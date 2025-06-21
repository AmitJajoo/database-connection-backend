package com.example.sql_ai.service;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class MongoDatabaseService implements DatabaseService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoDatabaseService(String uri, String dbName) {
        this.mongoClient = MongoClients.create(uri);
        this.database = mongoClient.getDatabase(dbName);
    }

    @Override
    public boolean testConnection() {
        try {
            database.listCollectionNames().first();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> executeQuery(String rawJson) {
        // Expecting: rawJson = collectionName:::filterJson
        String[] parts = rawJson.split(":::");
        String collectionName = parts[0];
        String filterJson = parts.length > 1 ? parts[1] : "{}";

        List<Map<String, Object>> result = new ArrayList<>();
        MongoCollection<Document> collection = database.getCollection(collectionName);

        Document filter = Document.parse(filterJson);

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                result.add(doc);
            }
        }

        return result;
    }

    @Override
    public List<String> listTablesOrCollections() {
        return database.listCollectionNames().into(new ArrayList<>());
    }
}

