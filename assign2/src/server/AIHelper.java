package server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AIHelper {
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final HttpClient client = HttpClient.newHttpClient();

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
                return "Error: Could not generate bot response. HTTP " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not generate bot response.";
        }
    }

    private static String buildRequestBody(String prompt, String context) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"llama3\",");
        json.append("\"prompt\":\"").append(escapeJson(prompt + "\n" + context)).append("\"");
        json.append("}");
        return json.toString();
    }

    private static String parseResponse(String responseBody) {
        int startIndex = responseBody.indexOf("\"response\":\"");
        if (startIndex == -1) return "Error: Bot response not found.";
        startIndex += 11;
        int endIndex = responseBody.indexOf("\"", startIndex);
        if (endIndex == -1) return "Error: Malformed bot response.";
        String raw = responseBody.substring(startIndex, endIndex);
        return raw.replace("\\n", "\n").replace("\\\"", "\"");
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
