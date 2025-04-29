package server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map; 
import java.util.HashMap;

public class AIHelper {
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getBotReply(String prompt, String context) {
        try {
            String requestBody = buildRequestBody(prompt, context);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                return "Error: Could not generate bot response.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not generate bot response.";
        }
    }

    private static String buildRequestBody(String prompt, String context) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama3");
            requestBody.put("prompt", prompt + "\n" + context);

            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    private static String parseResponse(String responseBody) {
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            return responseJson.path("response").asText(); // Extract the "response" field
        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing response from LLM.";
        }
    }
}
