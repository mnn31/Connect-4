import java.io.*;
import java.net.*;
import com.sun.net.httpserver.*;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.Executors;

public class ChatGPTGameServer {
    private static final int PORT = 8081;
    private ChatGPTGame game;

    public ChatGPTGameServer() {
        game = new ChatGPTGame();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        server.createContext("/move", new MoveHandler());
        server.createContext("/board", new BoardHandler());
        server.createContext("/reset", new ResetHandler());
        server.createContext("/ai-move", new AIMoveHandler());

        server.start();
        System.out.println("ChatGPT Game Server started on port " + PORT);
    }

    private class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                int column = Integer.parseInt(requestBody.split("=")[1]);
                
                boolean success = game.makeMove(column);
                String response = createGameStateResponse();
                sendResponse(exchange, response, 200);
            } catch (Exception e) {
                sendResponse(exchange, "Invalid move", 400);
            }
        }
    }

    private class BoardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }
            String response = createGameStateResponse();
            sendResponse(exchange, response, 200);
        }
    }

    private class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }
            game.reset();
            String response = createGameStateResponse();
            sendResponse(exchange, response, 200);
        }
    }

    private class AIMoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                sendResponse(exchange, "Method not allowed", 405);
                return;
            }

            try {
                int aiMove = game.getAIMove();
                if (aiMove != -1) {
                    game.makeMove(aiMove);
                }
                String response = createGameStateResponse();
                sendResponse(exchange, response, 200);
            } catch (Exception e) {
                sendResponse(exchange, "AI move failed", 400);
            }
        }
    }

    private String createGameStateResponse() {
        StringBuilder response = new StringBuilder();
        
        // Add board state
        int[][] board = game.getBoard();
        for (int[] row : board) {
            for (int cell : row) {
                response.append(cell).append(",");
            }
        }
        
        // Add game state
        response.append("|");
        response.append(game.isGameOver() ? "1" : "0").append(",");
        response.append(game.getWinner());
        
        // Add winning positions if game is over
        if (game.isGameOver() && game.getWinner() != 0) {
            response.append("|");
            int[][] winningPositions = game.getWinningPositions();
            if (winningPositions != null) {
                for (int[] pos : winningPositions) {
                    response.append(pos[0]).append(",").append(pos[1]).append(",");
                }
            }
        }
        
        return response.toString();
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

    public static void main(String[] args) throws IOException {
        ChatGPTGameServer server = new ChatGPTGameServer();
        server.start();
    }
} 