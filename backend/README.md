# Connect 4 Game AI System

## Overview

This document explains how the AI in our Connect 4 game works. The game includes two AI options:

1. **ChatGPT API Integration** - Attempts to use OpenAI's API (if an API key is available)
2. **Smart AI Strategy** - A custom-coded strategic AI algorithm (used as fallback or primary)

## How the Smart AI Works

The AI in this game is **not** a neural network or machine learning algorithm. Instead, it's a **hand-coded strategic algorithm** that we developed specifically for Connect 4. This approach is sometimes called a "rule-based AI" or "heuristic AI."

### Key Characteristics:

- **Coded from scratch**: The AI logic is written in Java and doesn't rely on external AI libraries
- **Deterministic**: It makes the same move given the same board state
- **Strategic**: Uses specific strategies for Connect 4 rather than general game-playing techniques

### How it Makes Decisions

The Smart AI uses a priority-based decision system:

1. **Win Detection**: First checks if it can win in one move
2. **Block Detection**: Checks if opponent can win in one move and blocks it
3. **Diagonal Threat Detection**: Looks for diagonal patterns that could lead to wins
4. **Strategic Scoring**: Evaluates each possible move using a scoring system:
   - Gives higher scores to moves that create connected pieces
   - Prioritizes center columns (better for Connect 4 strategy)
   - Assigns extra points to moves that block opponent setups
   - Heavily prioritizes blocking diagonal threats

## Technical Implementation

The AI is implemented in `ChatGPTAI.java` with these key methods:

- `getStrategicMove()`: Main decision-making method
- `findDiagonalThreats()`: Specialized logic to detect diagonal opportunities/threats
- `countConsecutive()`: Counts connected pieces in all directions
- `checkWin()`: Checks if a move would result in a win

## ChatGPT Integration

The system first attempts to use the ChatGPT API if an API key is available in the `.env` file. If the API call fails (due to connection issues, quota limits, etc.), the system automatically falls back to the Smart AI strategy.

## Maintenance and Improvements

The Smart AI can be enhanced by:

1. Adding more strategic patterns to recognize
2. Fine-tuning the scoring weights
3. Implementing difficulty levels
4. Adding more specialized detection for complex threats

## Summary

The AI in this game is a **custom-coded strategic algorithm** that analyzes the game board and makes decisions based on Connect 4-specific strategies and heuristics. It does not use neural networks, machine learning, or external AI libraries. 