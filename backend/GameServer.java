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
        
        // Add winning positions if game is over
        if (game.isGameOver() && game.getWinner() != 0) {
            int[][] winningPositions = game.getWinningPositions();
            if (winningPositions != null) {
                for (int[] pos : winningPositions) {
                    response.append(pos[0]).append(",").append(pos[1]).append(",");
                }
            }
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
            
            System.out.println("Player move request received");
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println("Request body: " + requestBody);
            
            String column = URLDecoder.decode(requestBody.split("=")[1], StandardCharsets.UTF_8);
            System.out.println("Player column selected: " + column);
            
            boolean validMove = game.makeMove(Integer.parseInt(column));
            System.out.println("Move valid: " + validMove);
            
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
            
            System.out.println("AI move request received");
            int aiMove = game.getAIMove();
            System.out.println("AI will move in column: " + aiMove);
            
            boolean validMove = game.makeMove(aiMove);
            System.out.println("AI move valid: " + validMove);
            
            String response = getBoardState();
            System.out.println("AI move response: " + response);
            
            exchange.getResponseHeaders().set("Content-Type", "text/plain");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            exchange.sendResponseHeaders(validMove ? 200 : 400, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            System.out.println("AI move response sent");
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