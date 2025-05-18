import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;

public class Connect4AI {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public Connect4AI() {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
        this.apiKey = loadApiKey();
    }

    private String loadApiKey() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("../.env"));
            return props.getProperty("OPENAI_API_KEY");
        } catch (IOException e) {
            System.err.println("Failed to load API key: " + e.getMessage());
            return null;
        }
    }

    public int getMove(int[][] board) {
        if (apiKey == null) {
            return getRandomMove(board);
        }

        try {
            String boardState = convertBoardToInput(board);
            String prompt = createPrompt(boardState);
            
            String response = callOpenAI(prompt);
            return processOpenAIResponse(response);
        } catch (Exception e) {
            System.err.println("AI prediction failed: " + e.getMessage());
            return getRandomMove(board);
        }
    }

    private String createPrompt(String boardState) {
        return String.format(
            "You are playing Connect 4. The current board state is: %s\n" +
            "Analyze the board and return only the column number (0-6) where you would place your piece.\n" +
            "Consider winning moves, blocking opponent's winning moves, and creating opportunities.\n" +
            "Return only the number, nothing else.",
            boardState
        );
    }

    private String callOpenAI(String prompt) throws IOException {
        String jsonBody = String.format(
            "{\"model\": \"%s\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}",
            MODEL, prompt
        );

        Request request = new Request.Builder()
            .url(OPENAI_API_URL)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
            .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            return response.body().string();
        }
    }

    private int processOpenAIResponse(String response) throws IOException {
        JsonNode root = objectMapper.readTree(response);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        
        try {
            int move = Integer.parseInt(content.trim());
            if (move >= 0 && move <= 6) {
                return move;
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid move format from AI: " + content);
        }
        
        return getRandomMove(null);
    }

    private String convertBoardToInput(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell).append(",");
            }
        }
        return sb.toString();
    }

    private int getRandomMove(int[][] board) {
        List<Integer> availableColumns = new ArrayList<>();
        for (int col = 0; col < 7; col++) {
            if (board == null || board[0][col] == 0) {
                availableColumns.add(col);
            }
        }
        
        if (availableColumns.isEmpty()) {
            return -1;
        }
        
        int randomIndex = (int) (Math.random() * availableColumns.size());
        return availableColumns.get(randomIndex);
    }

    public void close() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
} 