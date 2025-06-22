package com.example.sql_ai.service;

import com.example.sql_ai.dto.QueryRequest;

import java.util.List;
import java.util.Map;

public interface DatabaseService {
    boolean testConnection();
    List<Map<String, Object>> executeQuery(QueryRequest query);
    List<String> listTablesOrCollections();
}
