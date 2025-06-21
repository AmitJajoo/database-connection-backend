package com.example.sql_ai.controller;

import com.example.sql_ai.service.DatabaseService;
import com.example.sql_ai.service.JdbcDatabaseService;
import com.example.sql_ai.service.MongoDatabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UniversalDbController {

    @PostMapping("/query")
    public ResponseEntity<?> runQuery(@RequestBody Map<String, String> request) {
        try {
            DatabaseService service = getService(request);
            String query = request.getOrDefault("query", request.get("collection")); // For SQL use "query", for Mongo use "collection"
            List<Map<String, Object>> result = service.executeQuery(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tables")
    public ResponseEntity<?> listTablesOrCollections(@RequestBody Map<String, String> request) {
        try {
            DatabaseService service = getService(request);
            List<String> result = service.listTablesOrCollections();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/connect")
    public ResponseEntity<?> connect(@RequestBody Map<String, String> request) {
        try {
            DatabaseService service = getService(request);
            boolean connected = service.testConnection();
            if (connected) {
                return ResponseEntity.ok(Map.of("message", "Connection successful"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Connection failed"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    private DatabaseService getService(Map<String, String> request) {
        String type = request.get("type"); // "sql" or "mongo"

        if ("sql".equalsIgnoreCase(type)) {
            return new JdbcDatabaseService(
                    request.get("url"),
                    request.get("username"),
                    request.get("password")
            );
        } else if ("mongo".equalsIgnoreCase(type)) {
            return new MongoDatabaseService(
                    request.get("url"),
                    request.get("database")
            );
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}
