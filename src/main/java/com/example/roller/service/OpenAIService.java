/*package com.example.roller.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askGPT(String prompt) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Content-" + "Type", "application/json");
            System.out.println(apiKey);
           request.setHeader("Authorization", "Bearer " + apiKey);

            String requestBody = """
                        {
                            "model": "gpt-3.5-turbo",
                            "messages": [{"role": "user", "content": "%s"}],
                            "max_tokens": 150
                        }
                    """.formatted(prompt);


            request.setEntity(new StringEntity(requestBody, StandardCharsets.UTF_8));
            System.out.println(request);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                JsonNode responseJson = objectMapper.readTree(response.getEntity().getContent());
                System.out.println(responseJson);

                // Check if 'choices' array and 'message' field exist
                if (responseJson.has("choices") && responseJson.get("choices").isArray()) {
                    JsonNode choices = responseJson.get("choices");
                    if (choices.size() > 0 && choices.get(0).has("message")) {
                        return choices.get(0).get("message").get("content").asText();
                    } else {
                        return "Unexpected response format: 'message' field missing in choices.";
                    }
                } else if (responseJson.has("error")) {
                    // Handle errors from OpenAI API
                    return "Error from OpenAI API: " + responseJson.get("error").get("message").asText();
                } else {
                    return "Unexpected response format: 'choices' array missing.";
                }
            }
        }
    }
}
*/