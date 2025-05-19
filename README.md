# Connect 4 Game with Smart AI

## Overview

This is a Connect 4 game with a React frontend and Java backend. The game features a custom-built AI opponent that makes strategic decisions without using external machine learning libraries.

## Game Components

1. **React Frontend**: Modern web interface for playing the game
2. **Java Backend**: Game server that handles game logic and AI
3. **Smart AI System**: Custom-built strategic AI algorithm

## How the AI Works

Our Connect 4 AI is **entirely hand-coded** - we did not use ChatGPT to generate the moves during gameplay (although we can try to use it if you provide an API key).

### AI Explanation for Beginners

The AI in this game works like a chess player thinking ahead:

1. **Looking for Wins**: First, it checks if it can win right now
2. **Blocking Your Wins**: Then it checks if you're about to win and blocks you
3. **Finding Diagonal Threats**: It pays special attention to diagonal lines (which are harder to spot)
4. **Strategic Planning**: It evaluates every possible move by:
   - Counting how many pieces it would connect
   - Preferring the center columns (which offer more winning opportunities)
   - Blocking your potential setups
   
### Technical Details

- The AI is coded in Java (in the `ChatGPTAI.java` file)
- It uses a scoring system to evaluate potential moves
- It includes special detection for diagonal threats
- The code was **written from scratch**, not imported from an external source
- It does NOT use neural networks or machine learning techniques

## How to Run the Game

1. Start the backend server:
   ```
   cd backend
   javac -cp json-20210307.jar *.java
   java -cp ".:json-20210307.jar" GameServer
   ```

2. Start the React frontend:
   ```
   cd frontend
   npm start
   ```

3. Open your browser to `http://localhost:3000`

## Using ChatGPT API (Optional)

The game can attempt to use OpenAI's ChatGPT API to make moves if you provide an API key:

1. Create a `.env` file in the project root
2. Add your API key: `OPENAI_API_KEY=your_key_here`

If the API call fails (due to quota limits, etc.), the game will automatically fall back to the built-in Smart AI.

## Recent Improvements

The AI was recently enhanced to better detect and respond to diagonal threats - one of the most common ways players win in Connect 4.

## Project Development

This project was developed as part of an APCS course to demonstrate game development concepts and basic AI strategy implementation.
