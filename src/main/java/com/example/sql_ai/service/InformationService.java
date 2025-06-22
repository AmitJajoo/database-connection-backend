package com.example.sql_ai.service;

import com.example.sql_ai.dto.QueryRequest;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class InformationService {

    public Map<String, Object> getDbInfo(QueryRequest config) throws SQLException {
        Connection conn = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
        Map<String, Object> info = new HashMap<>();

        Statement stmt = conn.createStatement();
        ResultSet version = stmt.executeQuery("SELECT version()");
        if (version.next()) info.put("version", version.getString(1));

        ResultSet uptime = stmt.executeQuery("SELECT now() - pg_postmaster_start_time() as uptime");
        if (uptime.next()) info.put("uptime", uptime.getString("uptime"));

        ResultSet size = stmt.executeQuery("SELECT pg_size_pretty(pg_database_size(current_database())) as size");
        if (size.next()) info.put("dbSize", size.getString("size"));

        ResultSet connections = stmt.executeQuery("SELECT count(*) FROM pg_stat_activity");
        if (connections.next()) info.put("activeConnections", connections.getInt(1));

        conn.close();
        return info;
    }
}
