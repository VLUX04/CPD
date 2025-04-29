package server;

public class TestAI {
    public static void main(String[] args) {
        String response = AIHelper.getBotReply("What's the capital of France?", "");
        System.out.println("AI Response: " + response);
    }
}