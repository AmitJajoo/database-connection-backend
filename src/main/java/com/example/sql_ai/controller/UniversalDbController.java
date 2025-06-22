package com.example.sql_ai.controller;

import com.example.sql_ai.dto.QueryRequest;
import com.example.sql_ai.dto.SchemaRequest;
import com.example.sql_ai.service.DatabaseService;
import com.example.sql_ai.service.JdbcDatabaseService;
import com.example.sql_ai.service.MongoDatabaseService;
import com.example.sql_ai.service.SchemaService;
import com.example.sql_ai.util.CsvUtil;
import org.springframework.http.HttpHeaders;
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

    private final SchemaService schemaService;

    public UniversalDbController(SchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @PostMapping("/query")
    public ResponseEntity<?> runQuery(@RequestBody QueryRequest request) {
        try {
            DatabaseService service = getService(request);
            List<Map<String, Object>> result = service.executeQuery(request);

            // If CSV export is requested
            if (request.isExportCsv()) {
                String csv = CsvUtil.convertToCsv(result);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=data.csv")
                        .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                        .body(csv);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/tables")
    public ResponseEntity<?> listTablesOrCollections(@RequestBody QueryRequest request) {
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
    public ResponseEntity<?> connect(@RequestBody QueryRequest request) {
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

    @PostMapping("/schema")
    public ResponseEntity<?> getSchema(@RequestBody SchemaRequest request) {
        if ("sql".equalsIgnoreCase(request.getType())) {
            return ResponseEntity.ok(schemaService.fetchSqlSchema(request));
        } else if ("mongo".equalsIgnoreCase(request.getType())) {
            return ResponseEntity.ok(schemaService.fetchMongoSchema(request));
        } else {
            return ResponseEntity.badRequest().body("Unsupported DB type");
        }
    }



    private DatabaseService getService(QueryRequest request) {
        String type = request.getType(); // "sql" or "mongo"

        if ("sql".equalsIgnoreCase(type)) {
            return new JdbcDatabaseService(
                    request.getUrl(),
                    request.getUsername(),
                    request.getPassword()
            );
        } else if ("mongo".equalsIgnoreCase(type)) {
            return new MongoDatabaseService(
                    request.getUrl(),
                    request.getDatabase()
            );
        } else {
            throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }
}
