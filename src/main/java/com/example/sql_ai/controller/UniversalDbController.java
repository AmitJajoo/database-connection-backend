package com.example.sql_ai.controller;

import com.example.sql_ai.dto.QueryRequest;
import com.example.sql_ai.dto.SchemaRequest;
import com.example.sql_ai.service.*;
import com.example.sql_ai.util.CsvUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UniversalDbController {

    private final SchemaService schemaService;
    private final InformationService informationService;

    public UniversalDbController(SchemaService schemaService, InformationService informationService) {
        this.schemaService = schemaService;
        this.informationService = informationService;
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

    @PostMapping("/db-info")
    public ResponseEntity<Map<String, Object>> getDbInfo(@RequestBody QueryRequest config) throws SQLException {
        return ResponseEntity.ok(informationService.getDbInfo(config));
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
