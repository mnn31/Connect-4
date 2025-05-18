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

        // Check for potential diagonal threats (player is about to create a diagonal 3-in-a-row)
        int blockDiagonalCol = findDiagonalThreats(board, 1);
        if (blockDiagonalCol != -1) {
            return blockDiagonalCol;
        }

        // Check for potential diagonal opportunities for AI
        int createDiagonalCol = findDiagonalThreats(board, 2);
        if (createDiagonalCol != -1) {
            return createDiagonalCol;
        }

        // Try to create opportunities (look for moves that create 3 in a row)
        int bestCol = -1;
        int bestScore = -1;
        
        for (int col = 0; col < 7; col++) {
            if (isValidMove(board, col)) {
                int row = getNextRow(board, col);
                board[row][col] = 2; // Try AI's move
                
                // Get consecutive counts in all directions
                int consecutive = countConsecutive(board, row, col);
                int score = consecutive;
                
                // Prefer center column
                if (col == 3) score += 2;
                
                // Check if this move blocks opponent's potential setup
                board[row][col] = 0;  // Undo move
                board[row][col] = 1;  // Simulate player move here
                int opponentScore = countConsecutive(board, row, col);
                if (opponentScore >= 3) score += 5;  // Higher priority to blocking potential wins
                
                board[row][col] = 0;  // Undo move
                
                if (score > bestScore) {
                    bestScore = score;
                    bestCol = col;
                }
            }
        }
        
        if (bestCol != -1) {
            return bestCol;
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
        int horizCount = countDirection(board, row, col, 0, -1) + 
                         countDirection(board, row, col, 0, 1) + 1;
        maxCount = Math.max(maxCount, horizCount);
        
        // Check vertical
        int vertCount = countDirection(board, row, col, -1, 0) + 
                        countDirection(board, row, col, 1, 0) + 1;
        maxCount = Math.max(maxCount, vertCount);
        
        // Check diagonal (positive slope)
        int diagUpCount = countDirection(board, row, col, -1, -1) + 
                          countDirection(board, row, col, 1, 1) + 1;
        maxCount = Math.max(maxCount, diagUpCount);
        
        // Check diagonal (negative slope)
        int diagDownCount = countDirection(board, row, col, -1, 1) + 
                            countDirection(board, row, col, 1, -1) + 1;
        maxCount = Math.max(maxCount, diagDownCount);
        
        return maxCount;
    }
    
    // Helper method to count consecutive pieces in a direction
    private int countDirection(int[][] board, int row, int col, int rowDir, int colDir) {
        int player = board[row][col];
        int count = 0;
        
        // Move in the specified direction
        int r = row + rowDir;
        int c = col + colDir;
        
        // Count consecutive pieces of the same player
        while (r >= 0 && r < 6 && c >= 0 && c < 7 && board[r][c] == player) {
            count++;
            r += rowDir;
            c += colDir;
        }
        
        return count;
    }

    // Finds if a player can create a potential diagonal threat in the next move
    private int findDiagonalThreats(int[][] board, int player) {
        // Check diagonals (positive slope)
        for (int r = 0; r <= 5; r++) {
            for (int c = 0; c <= 6; c++) {
                int count = 0;
                
                // Count diagonal up-right (↗)
                if (r <= 2 && c <= 3) {
                    for (int i = 0; i < 3; i++) {
                        if (board[r+i][c+i] == player) {
                            count++;
                        } else if (board[r+i][c+i] != 0) {
                            count = -1; // Blocked by opponent
                            break;
                        }
                    }
                    
                    if (count == 2) {
                        // Check if we can place a piece to complete a threat
                        for (int i = 0; i < 3; i++) {
                            int checkRow = r+i;
                            int checkCol = c+i;
                            if (board[checkRow][checkCol] == 0) {
                                // Make sure there's either a piece below or it's the bottom row
                                if (checkRow == 5 || board[checkRow+1][checkCol] != 0) {
                                    return checkCol;
                                }
                            }
                        }
                    }
                }
                
                // Count diagonal down-right (↘)
                count = 0;
                if (r >= 3 && c <= 3) {
                    for (int i = 0; i < 3; i++) {
                        if (board[r-i][c+i] == player) {
                            count++;
                        } else if (board[r-i][c+i] != 0) {
                            count = -1; // Blocked by opponent
                            break;
                        }
                    }
                    
                    if (count == 2) {
                        // Check if we can place a piece to complete a threat
                        for (int i = 0; i < 3; i++) {
                            int checkRow = r-i;
                            int checkCol = c+i;
                            if (board[checkRow][checkCol] == 0) {
                                // Make sure there's either a piece below or it's the bottom row
                                if (checkRow == 5 || board[checkRow+1][checkCol] != 0) {
                                    return checkCol;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return -1;
    }
} 