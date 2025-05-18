import React from 'react';
import { Paper, Box, Button } from '@mui/material';
import { styled } from '@mui/material/styles';

const BoardContainer = styled(Paper)(({ theme }) => ({
  padding: theme.spacing(2),
  backgroundColor: '#1a237e',
  borderRadius: '16px',
  boxShadow: '0 8px 32px rgba(0, 0, 0, 0.3)',
  position: 'relative',
  overflow: 'visible'
}));

const Cell = styled(Box)(({ theme, isHovered }) => ({
  width: '60px',
  height: '60px',
  backgroundColor: '#0d47a1',
  borderRadius: '50%',
  margin: '4px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  cursor: isHovered ? 'pointer' : 'default',
  transition: 'all 0.3s ease',
  position: 'relative',
  '&:hover': {
    backgroundColor: isHovered ? '#1565c0' : '#0d47a1',
    transform: isHovered ? 'scale(1.05)' : 'none',
  }
}));

const Piece = styled(Box)(({ theme, player, isFalling, isWinning }) => ({
  width: '50px',
  height: '50px',
  borderRadius: '50%',
  backgroundColor: player === 1 ? theme.palette.primary.main : theme.palette.secondary.main,
  boxShadow: 'inset 0 0 20px rgba(0, 0, 0, 0.3)',
  position: 'relative',
  animation: isFalling ? 'fall 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards' : 'none',
  ...(isWinning && {
    animation: 'winPulse 1s infinite',
    boxShadow: '0 0 20px rgba(255, 255, 255, 0.8)',
  }),
  '@keyframes fall': {
    '0%': {
      transform: 'translateY(-400px)',
    },
    '100%': {
      transform: 'translateY(0)',
    },
  },
  '@keyframes winPulse': {
    '0%': {
      boxShadow: '0 0 0 0 rgba(255, 255, 255, 0.8)',
    },
    '70%': {
      boxShadow: '0 0 20px 10px rgba(255, 255, 255, 0.4)',
    },
    '100%': {
      boxShadow: '0 0 0 0 rgba(255, 255, 255, 0)',
    },
  },
}));

const GameBoard = ({ board, onColumnClick, gameOver, winner, onReset, winningPositions }) => {
  const [hoveredColumn, setHoveredColumn] = React.useState(null);

  const handleColumnHover = (column) => {
    if (!gameOver && board[0][column] === 0) {
      setHoveredColumn(column);
    }
  };

  const handleColumnLeave = () => {
    setHoveredColumn(null);
  };

  const handleColumnClick = (column) => {
    if (!gameOver && board[0][column] === 0) {
      onColumnClick(column);
    }
  };

  const isWinningPosition = (row, col) => {
    return winningPositions.some(pos => pos[0] === row && pos[1] === col);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
      <BoardContainer elevation={3}>
        <Box sx={{ display: 'flex', flexDirection: 'column' }}>
          {board.map((row, rowIndex) => (
            <Box key={rowIndex} sx={{ display: 'flex' }}>
              {row.map((cell, colIndex) => (
                <Cell
                  key={`${rowIndex}-${colIndex}`}
                  isHovered={hoveredColumn === colIndex && !gameOver}
                  onMouseEnter={() => handleColumnHover(colIndex)}
                  onMouseLeave={handleColumnLeave}
                  onClick={() => handleColumnClick(colIndex)}
                >
                  {cell !== 0 && (
                    <Piece 
                      player={cell} 
                      isFalling={true}
                      isWinning={gameOver && winner !== 0 && isWinningPosition(rowIndex, colIndex)}
                    />
                  )}
                </Cell>
              ))}
            </Box>
          ))}
        </Box>
      </BoardContainer>
      
      <Button 
        variant="contained" 
        color="primary" 
        onClick={onReset}
        sx={{ mt: 2 }}
      >
        New Game
      </Button>
    </Box>
  );
};

export default GameBoard;