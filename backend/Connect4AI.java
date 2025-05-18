import ai.djl.inference.Predictor;
import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Connect4AI {
    private ZooModel<Input, Output> model;
    private Predictor<Input, Output> predictor;

    public Connect4AI() {
        try {
            // Load a pre-trained model for game strategy
            Criteria<Input, Output> criteria = Criteria.builder()
                .setTypes(Input.class, Output.class)
                .optModelPath(Paths.get("models/connect4_model.pt"))
                .optProgress(new ProgressBar())
                .build();

            model = criteria.loadModel();
            predictor = model.newPredictor();
        } catch (Exception e) {
            System.err.println("Failed to load AI model: " + e.getMessage());
            // Fallback to random moves if model loading fails
        }
    }

    public int getMove(int[][] board) {
        try {
            if (predictor != null) {
                // Convert board state to model input
                Input input = new Input();
                input.add(convertBoardToInput(board));
                
                // Get prediction from model
                Output output = predictor.predict(input);
                return processModelOutput(output);
            }
        } catch (TranslateException e) {
            System.err.println("AI prediction failed: " + e.getMessage());
        }
        
        // Fallback to random move if AI fails
        return getRandomMove(board);
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

    private int processModelOutput(Output output) {
        // Process model output to get the best move
        String result = output.getAsString();
        String[] scores = result.split(",");
        
        int bestMove = 0;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < scores.length; i++) {
            double score = Double.parseDouble(scores[i]);
            if (score > bestScore) {
                bestScore = score;
                bestMove = i;
            }
        }
        
        return bestMove;
    }

    private int getRandomMove(int[][] board) {
        List<Integer> availableColumns = new ArrayList<>();
        for (int col = 0; col < board[0].length; col++) {
            if (board[0][col] == 0) {
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
        if (predictor != null) {
            predictor.close();
        }
        if (model != null) {
            model.close();
        }
    }
} 