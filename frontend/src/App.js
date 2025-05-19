import React, { useState, useEffect } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import { Container, Box, Typography, Alert, Button } from '@mui/material';
import GameBoard from './components/GameBoard';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#ffeb3b', // Yellow for player 1
    },
    secondary: {
      main: '#f50057', // Red for player 2 (AI)
    },
  },
});

function App() {
  const [board, setBoard] = useState(Array(6).fill().map(() => Array(7).fill(0)));
  const [gameOver, setGameOver] = useState(false);
  const [winner, setWinner] = useState(0);
  const [error, setError] = useState(null);
  const [isAIMove, setIsAIMove] = useState(false);
  const [winningPositions, setWinningPositions] = useState([]);

  useEffect(() => {
    fetchBoardState();
  }, []);

  // Add another useEffect to update status text when AI is thinking
  useEffect(() => {
    // This just ensures the UI updates when the AI is thinking
  }, [isAIMove]);

  const fetchBoardState = async () => {
    try {
      console.log("Fetching board state...");
      const response = await fetch('http://localhost:8080/board');
      const data = await response.text();
      console.log("Board data received:", data);
      
      const parts = data.split('|');
      const boardState = parts[0];
      
      // Parse game state - be careful with the format!
      let isOver = 0;
      let winnerValue = 0;
      
      if (parts.length > 1) {
        const gameStateParts = parts[1].split(',');
        if (gameStateParts.length >= 2) {
          isOver = parseInt(gameStateParts[0]);
          winnerValue = parseInt(gameStateParts[1]);
          console.log(`Game state parsed: isOver=${isOver}, winner=${winnerValue}`);
        }
      }
      
      // Parse board state
      const boardArray = boardState.split(',')
        .filter(cell => cell !== '')
        .map(Number);
      
      const newBoard = [];
      for (let i = 0; i < 6; i++) {
        newBoard.push(boardArray.slice(i * 7, (i + 1) * 7));
      }
      setBoard(newBoard);

      // Parse game state
      const gameIsOver = isOver === 1;
      setGameOver(gameIsOver);
      setWinner(winnerValue);
      console.log("Game state updated: gameOver =", gameIsOver, "winner =", winnerValue);

      // Parse winning positions if they exist
      if (parts.length > 2) {
        if (parts[2] === "draw") {
          console.log("Game ended in a draw");
          // Keep winningPositions empty for a draw
          setWinningPositions([]);
        } else if (isOver === 1 && winnerValue !== 0) {
          const positions = parts[2].split(',')
            .filter(pos => pos !== '')
            .map(Number);
          const winningPos = [];
          for (let i = 0; i < positions.length; i += 2) {
            if (i + 1 < positions.length) {
              winningPos.push([positions[i], positions[i + 1]]);
            }
          }
          setWinningPositions(winningPos);
        } else {
          setWinningPositions([]);
        }
      } else {
        setWinningPositions([]);
      }
      
      // Return whether the game is over
      return gameIsOver;
    } catch (error) {
      console.error('Error fetching board state:', error);
      return false;
    }
  };

  const makeMove = async (column) => {
    if (gameOver || isAIMove) {
      console.log("Move rejected: gameOver =", gameOver, "isAIMove =", isAIMove);
      return;
    }

    // Immediately set isAIMove to true to prevent double clicks
    setIsAIMove(true);
    console.log(`Player making move in column ${column}`);

    try {
      // Update board preview locally for immediate feedback
      const newBoard = JSON.parse(JSON.stringify(board)); // Deep copy
      let validMove = false;
      // Find lowest empty row in selected column
      for (let row = 5; row >= 0; row--) {
        if (newBoard[row][column] === 0) {
          // Create a copy of the board and place the player's piece
          newBoard[row][column] = 1; // Player is 1
          setBoard(newBoard);
          validMove = true;
          console.log(`Player piece placed at row ${row}, column ${column}`);
          break;
        }
      }

      if (!validMove) {
        console.log("Invalid move - column is full");
        setIsAIMove(false);
        return;
      }

      // Now send the move to the server
      const formData = new URLSearchParams();
      formData.append('column', column);

      console.log("Sending move to server...");
      const response = await fetch('http://localhost:8080/move', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData.toString(),
      });

      if (!response.ok) {
        throw new Error('Move failed');
      }

      // Get latest board state from server and check if game is over
      const isGameOver = await fetchBoardState();
      console.log("After player move, gameOver =", isGameOver);

      // Check if the game is over after player's move
      if (isGameOver) {
        console.log("Game over after player move");
        setIsAIMove(false);
        return;
      }

      // Make AI move with a small delay
      console.log("Triggering AI move in 1 second...");
      setTimeout(makeAIMove, 1000);
    } catch (error) {
      console.error('Error making move:', error);
      // Refresh board state to correct any inconsistencies
      fetchBoardState();
      setIsAIMove(false);
    }
  };

  const makeAIMove = async () => {
    console.log("🤖 AI MAKING MOVE - START");
    
    try {
      const aiResponse = await fetch('http://localhost:8080/ai-move', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });

      console.log("AI move response status:", aiResponse.status);
      
      if (!aiResponse.ok) {
        throw new Error(`AI move failed with status ${aiResponse.status}`);
      }
      
      const responseText = await aiResponse.text();
      console.log("AI move response body:", responseText);

      // Update the board
      await fetchBoardState();
    } catch (error) {
      console.error('❌ Error making AI move:', error);
      await fetchBoardState();
    } finally {
      console.log("🤖 AI MAKING MOVE - END");
      setIsAIMove(false);
    }
  };

  const resetGame = async () => {
    try {
      const response = await fetch('http://localhost:8080/reset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
      });

      if (!response.ok) {
        throw new Error('Reset failed');
      }

      await fetchBoardState();
      setGameOver(false);
      setWinner(0);
      setIsAIMove(false);
      setWinningPositions([]);
    } catch (error) {
      console.error('Error resetting game:', error);
    }
  };

  const getGameStatus = () => {
    if (gameOver) {
      if (winner === 0) {
        return "It's a Draw! 🤝";
      }
      return winner === 1 ? "You Won! 🎉" : "AI Won! 🤖";
    }
    return isAIMove ? "AI is thinking... 🤔" : "Your turn - Drop a piece! 👇";
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Container maxWidth="sm">
        <Box sx={{ 
          display: 'flex', 
          flexDirection: 'column', 
          alignItems: 'center', 
          minHeight: '100vh',
          py: 4
        }}>
          <Typography variant="h3" component="h1" gutterBottom>
            Connect 4
          </Typography>
          
          <Typography 
            variant="h5" 
            color="primary" 
            sx={{ 
              mb: 3,
              fontWeight: 'bold',
              textAlign: 'center'
            }}
          >
            {getGameStatus()}
          </Typography>

          <GameBoard 
            board={board} 
            onColumnClick={makeMove}
            gameOver={gameOver}
            winner={winner}
            onReset={resetGame}
            winningPositions={winningPositions}
          />
        </Box>
      </Container>
    </ThemeProvider>
  );
}

export default App; 