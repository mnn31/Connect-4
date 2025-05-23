import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GameServer {
    private static final int PORT = 8080;
    private ChatGPTGame game;

    public GameServer() {
        game = new ChatGPTGame();
        System.out.println("\n--------------------------------------------------");
        System.out.println("🎮 CONNECT 4 GAME SERVER");
        System.out.println("--------------------------------------------------");
        System.out.println("API Status: The system will attempt to use ChatGPT API first");
        System.out.println("           If API call fails, it will fall back to Smart AI");
        System.out.println("--------------------------------------------------\n");
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/board", exchange -> {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = getBoardState();
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.createContext("/move", new MoveHandler());

        server.createContext("/ai-move", new AIMoveHandler());

        server.createContext("/reset", exchange -> {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
                return;
            }
            if ("POST".equals(exchange.getRequestMethod())) {
                game.reset();
                String response = getBoardState();
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }

    private String getBoardState() {
        StringBuilder response = new StringBuilder();
        
        // Add board state
        int[][] board = game.getBoard();
        for (int[] row : board) {
            for (int cell : row) {
                response.append(cell).append(",");
            }
        }
        response.append("|");
        
        // Add game state
        response.append(game.isGameOver() ? "1" : "0").append(",").append(game.getWinner()).append("|");
        
        // Add winning positions if game is over with a winner (not a draw)
        if (game.isGameOver() && game.getWinner() > 0) {
            int[][] winningPositions = game.getWinningPositions();
            if (winningPositions != null) {
                for (int[] pos : winningPositions) {
                    response.append(pos[0]).append(",").append(pos[1]).append(",");
                }
            }
        } else if (game.isGameOver() && game.getWinner() == 0) {
            // It's a draw, add a specific indicator
            response.append("draw");
        }
        
        return response.toString();
    }

    private class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                System.out.println("OPTIONS request received for /move");
                sendResponse(exchange, "", 200);
                return;
            }
            if (!exchange.getRequestMethod().equals("POST")) {
                System.out.println("Invalid method for /move: " + exchange.getRequestMethod());
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }
            
            System.out.println("\n👤 Player move request received");
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Request body: " + requestBody);
            
            String column = URLDecoder.decode(requestBody.split("=")[1], StandardCharsets.UTF_8);
            System.out.println("Player column selected: " + column);
            
            System.out.println("Game status before move - Game over: " + game.isGameOver());
            boolean validMove = game.makeMove(Integer.parseInt(column));
            System.out.println("Move valid: " + validMove);
            System.out.println("Game status after move - Game over: " + game.isGameOver() + ", Winner: " + game.getWinner());
            
            String response = getBoardState();
            System.out.println("Response: " + response);
            
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            exchange.sendResponseHeaders(validMove ? 200 : 400, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            System.out.println("Player move response sent");
        }
    }

    private class AIMoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                System.out.println("OPTIONS request received for /ai-move");
                sendResponse(exchange, "", 200);
                return;
            }
            if (!exchange.getRequestMethod().equals("POST")) {
                System.out.println("Invalid method for /ai-move: " + exchange.getRequestMethod());
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }
            
            System.out.println("\n🤖🤖🤖 AI MOVE REQUEST RECEIVED 🤖🤖🤖");
            System.out.println("==================================================");
            
            try {
                System.out.println("Getting AI move from game...");
                int aiMove = game.getAIMove();
                System.out.println("AI selected column: " + aiMove);
                
                boolean validMove = false;
                if (aiMove != -1) {
                    System.out.println("Attempting to make AI move in column " + aiMove);
                    validMove = game.makeMove(aiMove);
                    System.out.println("AI move valid: " + validMove);
                } else {
                    System.out.println("AI returned invalid move -1!");
                }
                
                String response = getBoardState();
                System.out.println("AI move completed. Sending board state to client:");
                System.out.println(response);
                System.out.println("Game over status after move: " + game.isGameOver());
                System.out.println("Winner after move: " + game.getWinner());
                System.out.println("==================================================\n");
                
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
                
                exchange.sendResponseHeaders(validMove ? 200 : 400, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                System.out.println("AI move response sent to client");
            } catch (Exception e) {
                System.out.println("❌ ERROR PROCESSING AI MOVE: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, "AI move failed: " + e.getMessage(), 500);
            }
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static void main(String[] args) {
        try {
            new GameServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 