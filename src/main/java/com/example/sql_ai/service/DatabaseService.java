package com.example.sql_ai.service;

import java.util.List;
import java.util.Map;

public interface DatabaseService {
    boolean testConnection();
    List<Map<String, Object>> executeQuery(String query);
    List<String> listTablesOrCollections();
}
