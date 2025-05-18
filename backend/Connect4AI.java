import java.util.ArrayList;
import java.util.List;

public class Connect4AI {
    public int getMove(int[][] board) {
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
} 