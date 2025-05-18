import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import org.json.JSONObject;
import org.json.JSONArray;

public class ChatGPTAI {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String apiKey;
    private boolean usingChatGPT = true;

    public ChatGPTAI() {
        this.apiKey = loadApiKey();
        if (apiKey == null) {
            System.out.println("Warning: No API key found. Using random moves only.");
            usingChatGPT = false;
        }
    }

    private String loadApiKey() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream("../.env"));
            String key = props.getProperty("OPENAI_API_KEY");
            if (key == null || key.trim().isEmpty()) {
                System.out.println("Warning: API key is empty in .env file");
                return null;
            }
            return key.trim();
        } catch (Exception e) {
            System.err.println("Failed to load API key: " + e.getMessage());
            return null;
        }
    }

    public int getMove(int[][] board) {
        if (!usingChatGPT) {
            return getStrategicMove(board);
        }

        try {
            String boardState = convertBoardToInput(board);
            String prompt = createPrompt(boardState);
            
            String response = callChatGPT(prompt);
            return processResponse(response);
        } catch (Exception e) {
            System.err.println("ChatGPT API call failed: " + e.getMessage());
            System.out.println("Falling back to strategic moves");
            usingChatGPT = false;
            return getStrategicMove(board);
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

    private String callChatGPT(String prompt) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.2);
        requestBody.put("max_tokens", 100);

        System.out.println("Sending request to ChatGPT API...");
        System.out.println("Prompt: " + prompt);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String errorResponse = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse += responseLine;
                }
            }
            System.err.println("API Error Response: " + errorResponse);
            throw new Exception("API returned error code: " + responseCode + " - " + errorResponse);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        System.out.println("Received response from ChatGPT API");
        return response.toString();
    }

    private int processResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            String content = jsonResponse.getJSONArray("choices")
                                      .getJSONObject(0)
                                      .getJSONObject("message")
                                      .getString("content")
                                      .trim();
            
            int move = Integer.parseInt(content);
            if (move >= 0 && move <= 6) {
                return move;
            }
        } catch (Exception e) {
            System.err.println("Invalid response format: " + response);
        }
        
        return getStrategicMove(null);
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

    private int getStrategicMove(int[][] board) {
        // First, check for winning moves
        for (int col = 0; col < 7; col++) {
            if (isValidMove(board, col)) {
                int row = getNextRow(board, col);
                board[row][col] = 2; // Try AI's move
                if (checkWin(board, row, col)) {
                    board[row][col] = 0; // Undo move
                    return col;
                }
                board[row][col] = 0; // Undo move
            }
        }

        // Then, block opponent's winning moves
        for (int col = 0; col < 7; col++) {
            if (isValidMove(board, col)) {
                int row = getNextRow(board, col);
                board[row][col] = 1; // Try opponent's move
                if (checkWin(board, row, col)) {
                    board[row][col] = 0; // Undo move
                    return col;
                }
                board[row][col] = 0; // Undo move
            }
        }

        // Try to create opportunities (look for moves that create 3 in a row)
        for (int col = 0; col < 7; col++) {
            if (isValidMove(board, col)) {
                int row = getNextRow(board, col);
                board[row][col] = 2; // Try AI's move
                if (countConsecutive(board, row, col) >= 3) {
                    board[row][col] = 0; // Undo move
                    return col;
                }
                board[row][col] = 0; // Undo move
            }
        }

        // If no strategic moves, choose randomly from available columns
        List<Integer> availableColumns = new ArrayList<>();
        for (int col = 0; col < 7; col++) {
            if (isValidMove(board, col)) {
                availableColumns.add(col);
            }
        }
        
        if (availableColumns.isEmpty()) {
            return -1;
        }
        
        int randomIndex = (int) (Math.random() * availableColumns.size());
        return availableColumns.get(randomIndex);
    }

    private boolean isValidMove(int[][] board, int col) {
        return board[0][col] == 0;
    }

    private int getNextRow(int[][] board, int col) {
        for (int row = 5; row >= 0; row--) {
            if (board[row][col] == 0) {
                return row;
            }
        }
        return -1;
    }

    private boolean checkWin(int[][] board, int row, int col) {
        int player = board[row][col];
        
        // Check horizontal
        for (int c = 0; c <= 3; c++) {
            if (board[row][c] == player && 
                board[row][c+1] == player && 
                board[row][c+2] == player && 
                board[row][c+3] == player) {
                return true;
            }
        }
        
        // Check vertical
        for (int r = 0; r <= 2; r++) {
            if (board[r][col] == player && 
                board[r+1][col] == player && 
                board[r+2][col] == player && 
                board[r+3][col] == player) {
                return true;
            }
        }
        
        // Check diagonal (positive slope)
        for (int r = 0; r <= 2; r++) {
            for (int c = 0; c <= 3; c++) {
                if (board[r][c] == player && 
                    board[r+1][c+1] == player && 
                    board[r+2][c+2] == player && 
                    board[r+3][c+3] == player) {
                    return true;
                }
            }
        }
        
        // Check diagonal (negative slope)
        for (int r = 3; r <= 5; r++) {
            for (int c = 0; c <= 3; c++) {
                if (board[r][c] == player && 
                    board[r-1][c+1] == player && 
                    board[r-2][c+2] == player && 
                    board[r-3][c+3] == player) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private int countConsecutive(int[][] board, int row, int col) {
        int player = board[row][col];
        int maxCount = 0;
        
        // Check horizontal
        int count = 0;
        for (int c = 0; c < 7; c++) {
            if (board[row][c] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else {
                count = 0;
            }
        }
        
        // Check vertical
        count = 0;
        for (int r = 0; r < 6; r++) {
            if (board[r][col] == player) {
                count++;
                maxCount = Math.max(maxCount, count);
            } else {
                count = 0;
            }
        }
        
        // Check diagonal (positive slope)
        count = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                if (r + c == row + col && board[r][c] == player) {
                    count++;
                    maxCount = Math.max(maxCount, count);
                } else {
                    count = 0;
                }
            }
        }
        
        // Check diagonal (negative slope)
        count = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 7; c++) {
                if (r - c == row - col && board[r][c] == player) {
                    count++;
                    maxCount = Math.max(maxCount, count);
                } else {
                    count = 0;
                }
            }
        }
        
        return maxCount;
    }
} 