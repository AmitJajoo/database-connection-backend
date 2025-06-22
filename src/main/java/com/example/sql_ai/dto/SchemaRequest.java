package com.example.sql_ai.dto;

import lombok.Data;

@Data
public class SchemaRequest {
    private String type;
    private String url;
    private String username;
    private String password;
    private String database;
    private String table;      // for SQL
    private String collection; // for Mongo
}
