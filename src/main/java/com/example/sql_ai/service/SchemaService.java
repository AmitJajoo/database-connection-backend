package com.example.sql_ai.service;

import com.example.sql_ai.dto.SchemaRequest;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

@Service
public class SchemaService {

    public List<Map<String, String>> fetchSqlSchema(SchemaRequest request) {
        List<Map<String, String>> columns = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(request.getUrl(), request.getUsername(), request.getPassword())) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getColumns(null, null, request.getTable(), null);
            while (rs.next()) {
                Map<String, String> col = new HashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                columns.add(col);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch schema", e);
        }
        return columns;
    }

    public List<Map<String, String>> fetchMongoSchema(SchemaRequest request) {
        List<Map<String, String>> fields = new ArrayList<>();
        try (MongoClient mongoClient = MongoClients.create(request.getUrl())) {
            MongoDatabase db = mongoClient.getDatabase(request.getDatabase());
            MongoCollection<Document> collection = db.getCollection(request.getCollection());
            Document doc = collection.find().first();
            if (doc != null) {
                for (Map.Entry<String, Object> entry : doc.entrySet()) {
                    Map<String, String> field = new HashMap<>();
                    field.put("name", entry.getKey());
                    field.put("type", entry.getValue() == null ? "null" : entry.getValue().getClass().getSimpleName());
                    fields.add(field);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch schema", e);
        }
        return fields;
    }


}
