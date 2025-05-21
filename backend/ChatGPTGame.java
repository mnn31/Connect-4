public class ChatGPTGame {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int EMPTY = 0;
    private static final int PLAYER = 1;
    private static final int AI = 2;
    private int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private int winner;
    private int[][] winningPositions;
    private ChatGPTAI ai;

    public ChatGPTGame() {
        board = new int[ROWS][COLS];
        currentPlayer = 1;
        gameOver = false;
        winner = 0;
        winningPositions = null;
        ai = new ChatGPTAI();
    }

    public boolean makeMove(int col) {
        if (gameOver || col < 0 || col >= COLS) {
            System.out.println("Move rejected - Game over: " + gameOver + " or invalid column: " + col);
            return false;
        }

        System.out.println("Attempting move in column " + col + " by player " + currentPlayer);
        
        // Find the lowest empty row in the selected column
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = currentPlayer;
                System.out.println("Placed piece at row " + row + ", column " + col);
                
                // Check for win after making the move
                if (checkWin(row, col)) {
                    gameOver = true;
                    winner = currentPlayer;
                    System.out.println("GAME OVER: Player " + currentPlayer + " wins!");
                    return true;
                }
                
                // Check for draw if no win was detected
                if (isBoardFull()) {
                    gameOver = true;
                    winner = 0; // 0 indicates a draw
                    System.out.println("GAME OVER: It's a draw! The board is full.");
                    return true;
                }
                
                // Switch players only if no win or draw
                int previousPlayer = currentPlayer;
                currentPlayer = currentPlayer == 1 ? 2 : 1;
                System.out.println("Switching player from " + previousPlayer + " to " + currentPlayer);
                return true;
            }
        }
        System.out.println("Move rejected - column " + col + " is full.");
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

    // Check if the board is full (draw condition)
    private boolean isBoardFull() {
        for (int col = 0; col < COLS; col++) {
            if (board[0][col] == 0) {
                return false; // If any top row cell is empty, board is not full
            }
        }
        return true; // All columns are full
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
} 