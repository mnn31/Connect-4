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

  useEffect(() => {
    fetchBoardState();
  }, []);

  // Add effect to handle automatic reset
  useEffect(() => {
    let resetTimer;
    if (gameOver && winner !== 0) {
      console.log('Game over, setting reset timer...');
      // Show win message for 2 seconds before resetting
      resetTimer = setTimeout(async () => {
        console.log('Resetting game...');
        await resetGame();
      }, 2000);
    }
    return () => {
      if (resetTimer) {
        clearTimeout(resetTimer);
      }
    };
  }, [gameOver, winner]);

  const fetchBoardState = async () => {
    try {
      const response = await fetch('http://localhost:8080/board');
      const data = await response.text();
      const [boardState, gameState] = data.split('|');
      
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
      const [isOver, winnerValue] = gameState.split(',').map(Number);
      setGameOver(isOver);
      setWinner(winnerValue);
    } catch (error) {
      console.error('Error fetching board state:', error);
    }
  };

  const makeMove = async (column) => {
    if (gameOver || isAIMove) return;

    try {
      const formData = new URLSearchParams();
      formData.append('column', column);

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

      const data = await response.text();
      const [boardState, gameState] = data.split('|');
      
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
      const [isOver, winnerValue] = gameState.split(',').map(Number);
      setGameOver(isOver);
      setWinner(winnerValue);

      // If game is not over, trigger AI move after a delay
      if (!isOver) {
        setIsAIMove(true);
        setTimeout(() => {
          makeAIMove();
        }, 1000);
      }
    } catch (error) {
      console.error('Error making move:', error);
    }
  };

  const makeAIMove = async () => {
    try {
      // Find available columns
      const availableColumns = [];
      for (let col = 0; col < 7; col++) {
        if (board[0][col] === 0) {
          availableColumns.push(col);
        }
      }

      if (availableColumns.length > 0) {
        // Randomly select a column
        const randomCol = availableColumns[Math.floor(Math.random() * availableColumns.length)];
        
        const formData = new URLSearchParams();
        formData.append('column', randomCol);

        const response = await fetch('http://localhost:8080/move', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: formData.toString(),
        });

        if (!response.ok) {
          throw new Error('AI move failed');
        }

        const data = await response.text();
        const [boardState, gameState] = data.split('|');
        
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
        const [isOver, winnerValue] = gameState.split(',').map(Number);
        setGameOver(isOver);
        setWinner(winnerValue);
      }
    } catch (error) {
      console.error('Error making AI move:', error);
    } finally {
      setIsAIMove(false);
    }
  };

  const resetGame = async () => {
    try {
      console.log('Resetting game...');
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
    } catch (error) {
      console.error('Error resetting game:', error);
    }
  };

  const getGameStatus = () => {
    if (gameOver && winner !== 0) {
      return winner === 1 ? "You Win! 🎉" : "AI Wins! 🤖";
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
          />
        </Box>
      </Container>
    </ThemeProvider>
  );
}

export default App; 