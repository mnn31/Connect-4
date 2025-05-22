# Connect 4 Game with Smart AI

## Overview

This is a Connect 4 game with a React frontend and Java backend. The game features a custom-built AI opponent that makes strategic decisions without using external machine learning libraries.

## Game Components

1. **React Frontend**: Modern web interface for playing the game
2. **Java Backend**: Game server that handles game logic and AI
3. **Smart AI System**: Custom-built strategic AI algorithm

## How to Run the Game

### Prerequisites

- Java JDK 8 or higher
- Node.js and npm
- A web browser

### Backend Setup

1. Navigate to the backend directory:
   ```
   cd backend
   ```

2. Compile the Java files:
   ```
   javac -cp json-20210307.jar *.java
   ```

3. Start the server:
   ```
   java -cp ".:json-20210307.jar" GameServer
   ```
   - On Windows, use: `java -cp ".;json-20210307.jar" GameServer`

   The server will start on port 8080, and you should see a message indicating it's running.

### Frontend Setup

1. Open a new terminal and navigate to the frontend directory:
   ```
   cd frontend
   ```

2. Install the required dependencies:
   ```
   npm install
   ```

3. Start the React application:
   ```
   npm start
   ```

4. The game will automatically open in your default browser at `http://localhost:3000`

## Playing the Game

- Click on a column to drop your piece
- The AI will automatically make its move after you
- The game will announce when someone wins or when there's a draw
- Use the reset button to start a new game

## Using ChatGPT API (Optional)

The game can attempt to use OpenAI's ChatGPT API to make moves if you provide an API key:

1. Create a `.env` file in the project root
2. Add your API key: `OPENAI_API_KEY=your_key_here`

If the API call fails (due to quota limits, etc.), the game will automatically fall back to the built-in Smart AI.

## How the AI Works

Our Connect 4 AI is **entirely hand-coded** - we did not use ChatGPT to generate the moves during gameplay (although we can try to use it if you provide an API key).

### AI Strategy

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

## Troubleshooting

- If the backend fails to start, check if port 8080 is already in use
- If you see CORS errors in the browser console, ensure the backend server is running
- If the frontend cannot connect to the backend, verify the URLs in the React code match your backend configuration

## Project Development

This project was developed as part of an APCS course to demonstrate game development concepts and basic AI strategy implementation.
