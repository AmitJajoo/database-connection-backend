package com.example.sql_ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResponse {
    public List<Map<String, Object>> rows;
    public int totalCount;
}
