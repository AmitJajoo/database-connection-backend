package com.example.sql_ai.service;

import com.example.sql_ai.dto.QueryRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcDatabaseService implements DatabaseService {

    private final String url;
    private final String username;
    private final String password;

    public JdbcDatabaseService(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean testConnection() {
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public List<Map<String, Object>> executeQuery(QueryRequest request) {
        String baseQuery = request.getQuery();

        // Apply filters (basic where clause if filters are provided)
        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            String whereClause = request.getFilters().entrySet().stream()
                    .map(entry -> entry.getKey() + " = '" + entry.getValue() + "'")
                    .collect(Collectors.joining(" AND "));
            baseQuery += " WHERE " + whereClause;
        }

        // Apply sorting
        if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
            baseQuery += " ORDER BY " + request.getSortBy() + " " + request.getSortOrder();
        }

        // Apply pagination (MySQL/Postgres syntax)
        int offset = request.getPage() * request.getSize();
        baseQuery += " LIMIT " + request.getSize() + " OFFSET " + offset;

        // Execute
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(baseQuery);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();
            List<Map<String, Object>> rows = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                rows.add(row);
            }

            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("SQL Error: " + e.getMessage(), e);
        }
    }



    @Override
    public List<String> listTablesOrCollections() {
        List<String> tables = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to list tables: " + e.getMessage(), e);
        }

        return tables;
    }

}

