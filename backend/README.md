# Connect 4 Smart AI Implementation

## Overview

This project implements a Connect 4 game with a strategic AI opponent. The AI utilizes a combination of advanced strategies to make intelligent moves rather than just random selections. The implementation is located in the `ChatGPTAI.java` file, which provides both a fallback to OpenAI's ChatGPT API (when available) and a built-in strategic decision-making algorithm.

## AI Features

The AI uses a multi-layered strategy approach to determine the best move:

1. **Winning Move Detection**: The AI first checks if it can win in the next move by examining all possible placements and identifying any that would create a line of four.

2. **Blocking Opponent Wins**: If no immediate winning move is available, the AI checks if the opponent could win on their next turn and blocks those moves.

3. **Diagonal Threat Detection**: The AI looks for potential diagonal threats (three pieces in a row with an open spot that could complete a four) and prioritizes moves that either block or create these threats.

4. **Strategic Position Evaluation**: When no immediate threats exist, the AI evaluates each potential move using these criteria:
   - Counts consecutive pieces (2 or 3 in a row) to build toward a win
   - Prefers center columns (providing more winning opportunities)
   - Identifies and blocks potential setups by the opponent
   - Looks for opportunities to create multiple threats simultaneously

5. **ChatGPT Integration (Optional)**: If a valid OpenAI API key is provided in the `.env` file, the AI will first attempt to use ChatGPT for move decisions. If unavailable (due to API limits or missing key), it automatically falls back to the strategic algorithm.

## How the AI Works

### Strategic Move Selection Process

The `getStrategicMove` method implements the core decision-making logic:

```java
private int getStrategicMove(int[][] board) {
    // First, check for winning moves
    // Then, block opponent's winning moves
    // Check for potential diagonal threats
    // Try to create opportunities
    // If no strategic moves, choose randomly from available columns
}
```

### Board Evaluation

The AI uses several helper methods to evaluate the board state:

- `checkWin`: Determines if a move creates a winning position
- `countConsecutive`: Counts the number of consecutive pieces in all directions
- `findDiagonalThreats`: Identifies potential diagonal winning opportunities
- `countDirection`: Helper method to count pieces in a specific direction

### Performance

The strategic AI consistently makes smart moves that:
- Prioritize immediate wins
- Block opponent's winning moves
- Build toward winning positions
- Avoid moves that help the opponent

## Technical Implementation

The AI is integrated with the Connect 4 game server (`GameServer.java`) and the game logic (`ChatGPTGame.java`). The server responds to HTTP requests, allowing the frontend to communicate with the AI through a simple API.

## Using the AI

The AI is automatically utilized when playing against the computer. No additional configuration is needed to use the strategic AI features, though providing an OpenAI API key in a `.env` file (in the project root) will enable the ChatGPT-enhanced capabilities.

## Development History

The AI started as a simple random move generator but has evolved to include strategic decision making with multiple layers of analysis. The current implementation combines both rule-based strategies and potential integration with machine learning (via ChatGPT) to create a challenging opponent. 