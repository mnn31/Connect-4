public class Game {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int EMPTY = 0;
    private static final int PLAYER = 1;
    private static final int AI = 2;
    private static final int WINNING_SCORE = 100000;
    private static final int BLOCKING_SCORE = 10000;
    private static final int THREE_IN_ROW = 1000;
    private static final int TWO_IN_ROW = 100;
    private static final int MAX_DEPTH = 4;
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private int winner;
    private int[][] winningPositions;
    private Connect4AI ai;

    public Game() {
        board = new int[ROWS][COLS];
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        winningPositions = null;
        ai = new Connect4AI();
    }

    public boolean makeMove(int col) {
        if (gameOver || col < 0 || col >= COLS) {
            return false;
        }

        // Find the lowest empty row in the selected column
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = currentPlayer;
                
                // Check for win after making the move
                if (checkWin(row, col)) {
                    gameOver = true;
                    winner = currentPlayer;
                    return true;
                }
                
                // Switch players only if no win
                currentPlayer = currentPlayer == 1 ? 2 : 1;
                return true;
            }
        }
        return false;
    }

    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        
        // Check horizontal
        for (int c = 0; c <= COLS - 4; c++) {
            if (board[row][c] == player &&
                board[row][c + 1] == player &&
                board[row][c + 2] == player &&
                board[row][c + 3] == player) {
                winningPositions = new int[][] {
                    {row, c}, {row, c + 1}, {row, c + 2}, {row, c + 3}
                };
                return true;
            }
        }
        
        // Check vertical
        for (int r = 0; r <= ROWS - 4; r++) {
            if (board[r][col] == player &&
                board[r + 1][col] == player &&
                board[r + 2][col] == player &&
                board[r + 3][col] == player) {
                winningPositions = new int[][] {
                    {r, col}, {r + 1, col}, {r + 2, col}, {r + 3, col}
                };
                return true;
            }
        }
        
        // Check diagonal (positive slope)
        for (int r = 0; r <= ROWS - 4; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player &&
                    board[r + 1][c + 1] == player &&
                    board[r + 2][c + 2] == player &&
                    board[r + 3][c + 3] == player) {
                    winningPositions = new int[][] {
                        {r, c}, {r + 1, c + 1}, {r + 2, c + 2}, {r + 3, c + 3}
                    };
                    return true;
                }
            }
        }
        
        // Check diagonal (negative slope)
        for (int r = 3; r < ROWS; r++) {
            for (int c = 0; c <= COLS - 4; c++) {
                if (board[r][c] == player &&
                    board[r - 1][c + 1] == player &&
                    board[r - 2][c + 2] == player &&
                    board[r - 3][c + 3] == player) {
                    winningPositions = new int[][] {
                        {r, c}, {r - 1, c + 1}, {r - 2, c + 2}, {r - 3, c + 3}
                    };
                    return true;
                }
            }
        }
        
        return false;
    }

    private boolean isBoardFull() {
        for (int col = 0; col < COLS; col++) {
            if (board[0][col] == 0) {
                return false;
            }
        }
        return true;
    }

    public int[][] getBoard() {
        return board;
    }

    public void reset() {
        board = new int[ROWS][COLS];
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        winningPositions = null;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinner() {
        return winner;
    }

    public int[][] getWinningPositions() {
        return winningPositions;
    }

    public int getAIMove() {
        return ai.getMove(board);
    }

    public void cleanup() {
        if (ai != null) {
            ai.close();
        }
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || isGameOver()) {
            return evaluateBoard();
        }
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (int col = 0; col < COLS; col++) {
                if (isValidMove(col)) {
                    makeMove(col);
                    int score = minimax(depth - 1, false, alpha, beta);
                    undoMove(col);
                    maxScore = Math.max(maxScore, score);
                    alpha = Math.max(alpha, score);
                    if (beta <= alpha) break;
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int col = 0; col < COLS; col++) {
                if (isValidMove(col)) {
                    makeMove(col);
                    int score = minimax(depth - 1, true, alpha, beta);
                    undoMove(col);
                    minScore = Math.min(minScore, score);
                    beta = Math.min(beta, score);
                    if (beta <= alpha) break;
                }
            }
            return minScore;
        }
    }

    private int evaluateBoard() {
        int score = 0;
        
        // Evaluate horizontal
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                score += evaluateWindow(row, col, 0, 1);
            }
        }
        
        // Evaluate vertical
        for (int row = 0; row <= ROWS - 4; row++) {
            for (int col = 0; col < COLS; col++) {
                score += evaluateWindow(row, col, 1, 0);
            }
        }
        
        // Evaluate diagonal (positive slope)
        for (int row = 0; row <= ROWS - 4; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                score += evaluateWindow(row, col, 1, 1);
            }
        }
        
        // Evaluate diagonal (negative slope)
        for (int row = 3; row < ROWS; row++) {
            for (int col = 0; col <= COLS - 4; col++) {
                score += evaluateWindow(row, col, -1, 1);
            }
        }
        
        return score;
    }

    private int evaluateWindow(int startRow, int startCol, int rowDelta, int colDelta) {
        int aiCount = 0;
        int playerCount = 0;
        int emptyCount = 0;
        
        for (int i = 0; i < 4; i++) {
            int row = startRow + (i * rowDelta);
            int col = startCol + (i * colDelta);
            
            if (board[row][col] == AI) {
                aiCount++;
            } else if (board[row][col] == PLAYER) {
                playerCount++;
            } else {
                emptyCount++;
            }
        }
        
        if (aiCount == 4) return WINNING_SCORE;
        if (playerCount == 4) return -WINNING_SCORE;
        if (aiCount == 3 && emptyCount == 1) return THREE_IN_ROW;
        if (playerCount == 3 && emptyCount == 1) return -THREE_IN_ROW;
        if (aiCount == 2 && emptyCount == 2) return TWO_IN_ROW;
        if (playerCount == 2 && emptyCount == 2) return -TWO_IN_ROW;
        
        return 0;
    }

    private boolean isValidMove(int col) {
        return board[0][col] == EMPTY;
    }

    private int getLastMoveRow() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] != EMPTY) {
                    return row;
                }
            }
        }
        return -1;
    }

    private void undoMove(int col) {
        for (int row = 0; row < ROWS; row++) {
            if (board[row][col] != EMPTY) {
                board[row][col] = EMPTY;
                break;
            }
        }
        currentPlayer = currentPlayer == PLAYER ? AI : PLAYER;
    }
} 