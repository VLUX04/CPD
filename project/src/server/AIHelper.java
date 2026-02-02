package server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AIHelper {
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getBotReply(String prompt, String context) {
        try {
            if (prompt == null || prompt.isBlank()) {
                return "Error: Prompt is empty.";
            }

            if (context == null) {
                context = "";
            }

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
                System.err.println("Error response from API: " + response.body());
                return "Error: Could not generate bot response. HTTP " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not generate bot response.";
        }
    }

    private static String buildRequestBody(String prompt, String context) throws Exception {
        ObjectNode json = objectMapper.createObjectNode();
        json.put("model", "llama3"); // Make sure this matches the model available in Ollama
        json.put("prompt", prompt + "\n" + context);
        json.put("stream", false);
        return objectMapper.writeValueAsString(json);
    }

    private static String parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String reply = root.path("response").asText();
            if (reply == null || reply.isBlank()) {
                return "Error: Bot response not found.";
            }
            return reply.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not parse bot response.";
        }
    }
}
