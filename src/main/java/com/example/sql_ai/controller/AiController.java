package com.example.sql_ai.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@RequestMapping("/api")
public class AiController {
    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping(value = "/ai-sql", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, String> generateSql(@RequestBody String prompt) {
        String groqUrl = "https://api.groq.com/openai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(YOUR TOKEN); // <== Replace with your key

        prompt = "write a sql query "+prompt;
        Map<String, Object> body = Map.of(
                "model", "meta-llama/llama-4-scout-17b-16e-instruct",
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(groqUrl, HttpMethod.POST, request, Map.class);

        Map<String, Object> message = (Map<String, Object>) ((Map<String, Object>) ((List<?>) response.getBody().get("choices")).get(0)).get("message");
        String fullContent = (String) message.get("content");

        // Simple regex to extract SQL between ```sql and ```
        String query = "SELECT *"; // default fallback
        try {
            Matcher matcher = Pattern.compile("(?s)```sql\\s*(.*?)\\s*```").matcher(fullContent);
            if (matcher.find()) {
                query = matcher.group(1);
            }
        } catch (Exception e) {
            query = "SELECT * FROM fallback_table;";
        }

        return Map.of("query", query.trim());
    }

}
