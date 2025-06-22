package com.example.sql_ai.service;

import com.example.sql_ai.dto.QueryRequest;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<Map<String, Object>> executeQuery(QueryRequest request) {
        MongoClient client = MongoClients.create(request.getUrl());
        MongoDatabase db = client.getDatabase(request.getDatabase());
        String[] parts = request.getQuery().split(":::");
        String collectionName = parts[0];
        String rawQuery = parts.length > 1 ? parts[1] : "{}";

        MongoCollection<Document> collection = db.getCollection(collectionName);
        Bson filter = BsonDocument.parse(rawQuery);

        // Add filters from UI
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            Bson extraFilters = Filters.and(
                    request.getFilters().entrySet().stream()
                            .map(e -> Filters.eq(e.getKey(), e.getValue()))
                            .collect(Collectors.toList())
            );
            filter = Filters.and(filter, extraFilters);
        }

        FindIterable<Document> result = collection.find(filter);

        // Sort
        if (request.getSortBy() != null) {
            result = result.sort(Sorts.orderBy(
                    "desc".equalsIgnoreCase(request.getSortOrder())
                            ? Sorts.descending(request.getSortBy())
                            : Sorts.ascending(request.getSortBy())));
        }

        // Pagination
        result = result.skip(request.getPage() * request.getSize()).limit(request.getSize());

        List<Map<String, Object>> docs = new ArrayList<>();
        for (Document doc : result) {
            docs.add(doc);
        }

        client.close();
        return docs;
    }


    @Override
    public List<String> listTablesOrCollections() {
        return database.listCollectionNames().into(new ArrayList<>());
    }
}

