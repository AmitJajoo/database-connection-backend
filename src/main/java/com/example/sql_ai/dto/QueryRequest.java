package com.example.sql_ai.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QueryRequest {
    private String type;       // "sql" or "mongo"
    private String url;
    private String username;
    private String password;
    private String database;
    private String query;      // For SQL
    private String collection; // For Mongo
    private Map<String, Object> filters;
    private String sortBy;
    private String sortOrder;  // "asc" or "desc"
    private int page = 0;
    private int size = 50;
    private boolean exportCsv = false;
}
