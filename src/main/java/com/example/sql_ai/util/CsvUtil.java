package com.example.sql_ai.util;

import java.util.*;

public class CsvUtil {
    public static String convertToCsv(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        List<String> headers = new ArrayList<>(rows.get(0).keySet());
        sb.append(String.join(",", headers)).append("\n");

        for (Map<String, Object> row : rows) {
            for (int i = 0; i < headers.size(); i++) {
                Object val = row.get(headers.get(i));
                sb.append(val == null ? "" : val.toString().replace(",", " "));
                if (i != headers.size() - 1) sb.append(",");
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
