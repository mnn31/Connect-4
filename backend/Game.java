public class Game {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private int winner;

    public Game() {
        board = new int[ROWS][COLS];
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
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
                return true;
            }
        }
        
        // Check vertical
        for (int r = 0; r <= ROWS - 4; r++) {
            if (board[r][col] == player &&
                board[r + 1][col] == player &&
                board[r + 2][col] == player &&
                board[r + 3][col] == player) {
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
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinner() {
        return winner;
    }
} 