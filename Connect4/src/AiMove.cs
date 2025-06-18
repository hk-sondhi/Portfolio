//script influenced and adapted from tutorial by octoman 2021
//Udemy. (n.d.) Connect 4 Game Programming for Unity 3D. Udemy [Online]. Available at: https://www.udemy.com/course/connect-4-game-programming-course-for-unity-3d/ (Accessed: 10 Nov 2024).
// The code has been adapted and expanded to fit the specific requirements of this project.

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using System.Diagnostics;
using System.Runtime.CompilerServices;
using System.Text; 
using Debug = UnityEngine.Debug;


// Implements the AI opponent for the Connect Four game using Minimax algorithm
// with alpha-beta pruning. This class manages all AI decision-making processes.

public class AiMove : MonoBehaviour
{
    public static AiMove instance;
    
    // Board dimensions 
    private static readonly int Width = 7;
    private static readonly int Height = 6;

    // Player identifiers
    private const int EMPTY = 0;
    private const int PLAYER_ONE = 1;
    private const int PLAYER_TWO = 2;

    // Scoring constants
    private const float WIN_SCORE = 1000f;
    private const float THREE_IN_A_ROW_SCORE = 5f;
    private const float TWO_IN_A_ROW_SCORE = 1f;
    private const float CENTER_COLUMN_BONUS = 0.5f; 

    // Search depth controlled by difficulty level
    private int maxSearch;


    [SerializeField, Tooltip("Enable to see debug logs")]
    private bool showDebugLogs = false;
    
    [SerializeField, Tooltip("Enable to see performance metrics")]
    private bool showPerformanceMetrics = false;
    
    [SerializeField, Range(0.1f, 2f), Tooltip("Artificial delay in seconds to make AI move more visible")]
    private float artificialMoveDelay = 0.5f;

    // Reusable arrays and collections to avoid garbage collection
    private int[,] tempBoardBuffer;
    private List<Move> movesBuffer = new List<Move>(7);
    
    // Performance tracking
    private int positionsEvaluated = 0;
    private int pruningCuts = 0;
    
    // Move history for potential analysis
    private List<MoveAnalysis> moveHistory = new List<MoveAnalysis>(42); // Max possible moves in a game
    
    
    // Data structure to represent a move 
    public struct Move
    {
        public int column;
        public int row;
        public float score;

        public Move(float _score)
        {
            column = -1;
            row = -1;
            score = _score;
        }

        public Move(int _row, int _column)
        {
            row = _row;
            column = _column;
            score = 0;
        }

        public Move(int _row, int _column, float _score)
        {
            row = _row;
            column = _column;
            score = _score;
        }
        
        public override string ToString()
        {
            return $"Move[col={column}, row={row}, score={score}]";
        }
    }
    
    
    // Structure to track move analysis data
    
    private struct MoveAnalysis
    {
        public int column;
        public int searchDepth;
        public float score;
        public long timeTaken;
        public int positionsEvaluated;
        
        public MoveAnalysis(int col, int depth, float scr, long time, int positions)
        {
            column = col;
            searchDepth = depth;
            score = scr;
            timeTaken = time;
            positionsEvaluated = positions;
        }
    }

    void Awake()
    {
        // Singleton pattern implementation
        if (instance != null && instance != this)
        {
            Destroy(gameObject);
            return;
        }
        instance = this;
        tempBoardBuffer = new int[Height, Width];
    }

    void Start()
    {
        // Set max search depth from difficulty setting
        maxSearch = Difficulty.difficulty;
        if (maxSearch <= 0)
        {
            Debug.LogWarning("AiMove: Difficulty level is set to 0, AI may not function as expected.");
        }
    }

    
    // Creates a debug string representation of the board
    
    string DebugBoard(int[,] board)
    {
        // Use StringBuilder for more efficient string construction
        StringBuilder sb = new StringBuilder(Height * (Width * 2 + 3));
        string seperator = ",";
        string border = "|";

        for (int x = 0; x < Height; x++)
        {
            sb.Append(border);
            for (int y = 0; y < Width; y++)
            {
                sb.Append(board[x, y]);
                sb.Append(seperator);
            }
            sb.Append(border);
            sb.Append('\n');
        }
        return sb.ToString();
    }

    
    // Logs a message if debug logging is enabled
    
    [Conditional("UNITY_EDITOR")]
    private void DebugLog(string message)
    {
        if (showDebugLogs)
        {
            Debug.Log($"[AiMove] {message}");
        }
    }

    
    // Scans the board and finds the topmost empty row for each column
    
    List<Move> GetValidMoves(int[,] currentBoard)
    {
        // Clear and reuse the moves buffer list to reduce garbage collection
        movesBuffer.Clear();
        
        // First check center column (tends to be stronger)
        int centerColumn = Width / 2;
        CheckColumnForValidMove(currentBoard, centerColumn, movesBuffer);
        
        // Then check columns adjacent to center, alternating outward
        for (int offset = 1; offset <= Width / 2; offset++)
        {
            int leftColumn = centerColumn - offset;
            int rightColumn = centerColumn + offset;
            
            // Check left side
            if (leftColumn >= 0)
            {
                CheckColumnForValidMove(currentBoard, leftColumn, movesBuffer);
            }
            
            // Check right side
            if (rightColumn < Width)
            {
                CheckColumnForValidMove(currentBoard, rightColumn, movesBuffer);
            }
        }
        return movesBuffer;
    }
    
    
    // Helper method to check a column for valid moves
    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    private void CheckColumnForValidMove(int[,] board, int column, List<Move> moveList)
    {
        // Start from the bottom of the column (top row in array)
        for (int row = Height - 1; row >= 0; row--)
        {
            if (board[row, column] == EMPTY)
            {
                moveList.Add(new Move(row, column));
                break; // Only the top most empty cell matters per column
            }
        }
    }

    
    // Called by GameManager when it is the AI's turn
    public void BestMove()
    {
        if (GameManager.instance == null)
        {
            Debug.LogError("AiMove: GameManager instance is null");
            return;
        }
        
        // Start timing
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.Start();
        
        // Reset counters
        positionsEvaluated = 0;
        pruningCuts = 0;
        
        // Start with worst score
        float bestScore = -Mathf.Infinity;
        int bestColumn = -1;
        
        // Get current board
        int[,] currentPlayfield = Playfield.instance.CurrentPlayfield();
        if (currentPlayfield == null)
        {
            Debug.LogError("AiMove: Current playfield is null");
            return;
        }

        // All valid columns
        List<Move> possibleMoves = GetValidMoves(currentPlayfield);
        
        if (possibleMoves.Count == 0)
        {
            Debug.LogWarning("AiMove: No valid moves available");
            return;
        }
        
        // If only one move is possible, take it without calculation
        if (possibleMoves.Count == 1)
        {
            DebugLog("Only one move available, taking it immediately");
            
            // If delay is enabled, use a coroutine to delay the move
            if (artificialMoveDelay > 0)
            {
                StartCoroutine(DelayedMove(possibleMoves[0].column));
            }
            else
            {
                GameManager.instance.ColumnPressed(possibleMoves[0].column);
            }
            return;
        }

        // Main AI decision making loop
        for (int i = 0; i < possibleMoves.Count; i++)
        {
            Move move = possibleMoves[i];

            // Try the move temporarily as AI (player 2)
            int[,] tempBoard = PerformTempMove(move, currentPlayfield, PLAYER_TWO);
            
            // Score it using Minimax with pruning
            float score = Minimax(tempBoard, maxSearch, -Mathf.Infinity, Mathf.Infinity, false);
            
            // Apply small bonus for center column to prefer strategic positions
            if (move.column == Width / 2)
            {
                score += CENTER_COLUMN_BONUS;
            }
            
            DebugLog($"Move {move.column}, Score: {score}");

            // Keep track of the best scoring move
            if (score > bestScore)
            {
                bestScore = score;
                bestColumn = move.column;
            }
        }
        
        // Stop timing
        stopwatch.Stop();
        
        // Log performance metrics
        if (showPerformanceMetrics)
        {
            Debug.Log($"[AiMove] Decision time: {stopwatch.ElapsedMilliseconds}ms, Positions evaluated: {positionsEvaluated}, Pruning cuts: {pruningCuts}");
        }
        
        // Record move for analysis
        moveHistory.Add(new MoveAnalysis(
            bestColumn, 
            maxSearch, 
            bestScore, 
            stopwatch.ElapsedMilliseconds, 
            positionsEvaluated));
        
        // Make the move with optional delay
        if (artificialMoveDelay > 0)
        {
            StartCoroutine(DelayedMove(bestColumn));
        }
        else
        {
            GameManager.instance.ColumnPressed(bestColumn);
        }
    }
    
    
    // Coroutine to add a delay before making a move
    private IEnumerator DelayedMove(int column)
    {
        yield return new WaitForSeconds(artificialMoveDelay);
        GameManager.instance.ColumnPressed(column);
    }

    
    // Copies the current board and simulates placing a piece
    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    int[,] PerformTempMove(Move move, int[,] currentBoard, int player)
    {
        // Copy the current board to our buffer
        System.Array.Copy(currentBoard, tempBoardBuffer, currentBoard.Length);
        
        // Place the ghost move
        tempBoardBuffer[move.row, move.column] = player;
        
        return tempBoardBuffer;
    }

    
    // Recursively evaluates moves using Minimax and alpha-beta pruning
    float Minimax(int[,] currentBoard, int searchDepth, float alpha, float beta, bool isMaximiser)
    {
        // Count this position
        positionsEvaluated++;
        
        // Early termination - check for win
        bool isWinPosition = CheckForWin(currentBoard);
        if (isWinPosition)
        {
            return isMaximiser ? -WIN_SCORE : WIN_SCORE;
        }
        
        // Base case - reached max depth
        if (searchDepth == 0)
        {
            return EvaluateBoard(currentBoard, isMaximiser);
        }

        // Get all possible next moves
        List<Move> possibleMoves = GetValidMoves(currentBoard);
        
        // If no moves available, it's a draw
        if (possibleMoves.Count == 0)
        {
            return 0;
        }

        float bestScore;
        if (isMaximiser)
        {
            bestScore = -Mathf.Infinity;
            for (int i = 0; i < possibleMoves.Count; i++)
            {
                Move move = possibleMoves[i];
                
                // Create a temporary board with this move
                int[,] tempBoard = new int[Height, Width];
                System.Array.Copy(currentBoard, tempBoard, currentBoard.Length);
                tempBoard[move.row, move.column] = PLAYER_TWO;
                
                float score = Minimax(tempBoard, searchDepth - 1, alpha, beta, !isMaximiser);
                bestScore = Mathf.Max(bestScore, score);

                // Alpha-Beta Pruning
                alpha = Mathf.Max(alpha, bestScore);
                if (beta <= alpha)
                {
                    pruningCuts++; // Track pruning effectiveness
                    break;
                }
            }
        }
        else // IS MINIMISER
        {
            bestScore = Mathf.Infinity;
            for (int i = 0; i < possibleMoves.Count; i++)
            {
                Move move = possibleMoves[i];
                
                // Create a temporary board with this move
                int[,] tempBoard = new int[Height, Width];
                System.Array.Copy(currentBoard, tempBoard, currentBoard.Length);
                tempBoard[move.row, move.column] = PLAYER_ONE;
                
                float score = Minimax(tempBoard, searchDepth - 1, alpha, beta, !isMaximiser);
                bestScore = Mathf.Min(bestScore, score);

                // Alpha-Beta Pruning
                beta = Mathf.Min(beta, bestScore);
                if (beta <= alpha)
                {
                    pruningCuts++; // Track pruning effectiveness
                    break;
                }
            }
        }
        return bestScore;
    }

    
    // Quickly checks if the current board has a winning position
    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    private bool CheckForWin(int[,] board)
    {
        // Check horizontally
        for (int row = 0; row < Height; row++)
        {
            for (int col = 0; col <= Width - 4; col++)
            {
                int cell = board[row, col];
                if (cell != EMPTY && 
                    cell == board[row, col+1] && 
                    cell == board[row, col+2] && 
                    cell == board[row, col+3])
                {
                    return true;
                }
            }
        }
        
        // Check vertically
        for (int row = 0; row <= Height - 4; row++)
        {
            for (int col = 0; col < Width; col++)
            {
                int cell = board[row, col];
                if (cell != EMPTY && 
                    cell == board[row+1, col] && 
                    cell == board[row+2, col] && 
                    cell == board[row+3, col])
                {
                    return true;
                }
            }
        }
        
        // Check diagonally (top-left to bottom-right)
        for (int row = 0; row <= Height - 4; row++)
        {
            for (int col = 0; col <= Width - 4; col++)
            {
                int cell = board[row, col];
                if (cell != EMPTY && 
                    cell == board[row+1, col+1] && 
                    cell == board[row+2, col+2] && 
                    cell == board[row+3, col+3])
                {
                    return true;
                }
            }
        }
        
        // Check diagonally (bottom-left to top-right)
        for (int row = 3; row < Height; row++)
        {
            for (int col = 0; col <= Width - 4; col++)
            {
                int cell = board[row, col];
                if (cell != EMPTY && 
                    cell == board[row-1, col+1] && 
                    cell == board[row-2, col+2] && 
                    cell == board[row-3, col+3])
                {
                    return true;
                }
            }
        }
        
        return false;
    }

    
    // Scores the current board based on potential winning patterns
    float EvaluateBoard(int[,] currentBoard, bool isMaximiser)
    {
        float boardScore = 0;

        boardScore += HorizontalCheck(currentBoard, isMaximiser);
        boardScore += VerticalCheck(currentBoard, isMaximiser);
        boardScore += DiagonalCheck(currentBoard, isMaximiser);
        boardScore += CenterControlScore(currentBoard, isMaximiser); // Additional evaluation for center control

        if (showDebugLogs)
        {
            DebugLog($"Board score: {boardScore}");
        }

        return boardScore;
    }
    
    
    // Adds a small bonus for controlling the center columns
    private float CenterControlScore(int[,] board, bool isMaximiser)
    {
        float score = 0;
        int centerColumn = Width / 2;
        
        // Check center column for pieces
        for (int row = 0; row < Height; row++)
        {
            int cell = board[row, centerColumn];
            if (cell != EMPTY)
            {
                if (isMaximiser)
                {
                    score += (cell == PLAYER_TWO) ? 0.1f : -0.1f;
                }
                else
                {
                    score += (cell == PLAYER_ONE) ? 0.1f : -0.1f;
                }
            }
        }
        
        return score;
    }

    
    // Helper method to evaluate a pattern of four cells
    [MethodImpl(MethodImplOptions.AggressiveInlining)]
    private float EvaluatePattern(int a, int b, int c, int d, bool isMaximiser)
    {
        // Skip empty patterns
        if (a == EMPTY && b == EMPTY && c == EMPTY && d == EMPTY)
        {
            return 0;
        }
        
        float score = 0;
        
        // WIN CHECK
        if (a == b && a == c && a == d && a != EMPTY)
        {
            if (isMaximiser)
            {
                score += (a == PLAYER_ONE) ? -WIN_SCORE : WIN_SCORE;
            }
            else // MINIMISER
            {
                score += (a == PLAYER_TWO) ? WIN_SCORE : -WIN_SCORE;
            }
        }
        // THREE CHECK - BUT CAN BE A FOUR
        else if (a == b && a == c && d == EMPTY && a != EMPTY)
        {
            if (isMaximiser)
            {
                score += (a == PLAYER_ONE) ? -THREE_IN_A_ROW_SCORE : THREE_IN_A_ROW_SCORE;
            }
            else // MINIMISER
            {
                score += (a == PLAYER_TWO) ? THREE_IN_A_ROW_SCORE : -THREE_IN_A_ROW_SCORE;
            }
        }
        // TWO CHECK - BUT CAN BE A FOUR
        else if (a == b && c == EMPTY && d == EMPTY && a != EMPTY)
        {
            if (isMaximiser)
            {
                score += (a == PLAYER_ONE) ? -TWO_IN_A_ROW_SCORE : TWO_IN_A_ROW_SCORE;
            }
            else // MINIMISER
            {
                score += (a == PLAYER_TWO) ? TWO_IN_A_ROW_SCORE : -TWO_IN_A_ROW_SCORE;
            }
        }
        return score;
    }

    
    // Checks and scores vertical patterns
    float VerticalCheck(int[,] currentBoard, bool isMaximiser)
    {
        float score = 0;

        for (int column = 0; column < Width; column++)
        {
            for (int row = Height-1; row >= 3; row--) // Only check rows where vertical 4-in-a-row is possible
            {
                int a = currentBoard[row, column];
                if (a == EMPTY) continue; // Skip empty cells for efficiency
                
                int b = currentBoard[row-1, column];
                int c = currentBoard[row-2, column];
                int d = currentBoard[row-3, column];

                score += EvaluatePattern(a, b, c, d, isMaximiser);
            }
        }
        return score;
    }

    
    // Checks and scores horizontal patterns
    float HorizontalCheck(int[,] currentBoard, bool isMaximiser)
    {
        float score = 0;

        for (int row = 0; row < Height; row++)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                // Check every possible horizontal window of 4
                int a = currentBoard[row, column];
                if (a == EMPTY && 
                    currentBoard[row, column+1] == EMPTY && 
                    currentBoard[row, column+2] == EMPTY && 
                    currentBoard[row, column+3] == EMPTY)
                {
                    continue; // Skip entirely empty windows for efficiency
                }
                
                int b = currentBoard[row, column+1];
                int c = currentBoard[row, column+2];
                int d = currentBoard[row, column+3];
                
                score += EvaluatePattern(a, b, c, d, isMaximiser);
            }
        }
        return score;
    }

    
    // Checks and scores diagonal patterns
    float DiagonalCheck(int[,] currentBoard, bool isMaximiser)
    {
        float score = 0;

        // Check diagonals (top-left to bottom-right)
        for (int row = 0; row <= Height - 4; row++)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                int a = currentBoard[row, column];
                int b = currentBoard[row+1, column+1];
                int c = currentBoard[row+2, column+2];
                int d = currentBoard[row+3, column+3];
                
                // Skip if all empty
                if (a == EMPTY && b == EMPTY && c == EMPTY && d == EMPTY)
                {
                    continue;
                }
                
                score += EvaluatePattern(a, b, c, d, isMaximiser);
            }
        }
        
        // Check diagonals (bottom-left to top-right)
        for (int row = 3; row < Height; row++)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                int a = currentBoard[row, column];
                int b = currentBoard[row-1, column+1];
                int c = currentBoard[row-2, column+2];
                int d = currentBoard[row-3, column+3];
                
                // Skip if all empty
                if (a == EMPTY && b == EMPTY && c == EMPTY && d == EMPTY)
                {
                    continue;
                }
                
                score += EvaluatePattern(a, b, c, d, isMaximiser);
            }
        }
        return score;
    }
    
    
    // For debugging: Dump the AI move history to console
    public void DumpMoveHistory()
    {
        StringBuilder sb = new StringBuilder();
        sb.AppendLine("AI Move History:");
        sb.AppendLine("Column | Depth | Score | Time(ms) | Positions");
        
        foreach (var move in moveHistory)
        {
            sb.AppendLine($"{move.column} | {move.searchDepth} | {move.score:F2} | {move.timeTaken} | {move.positionsEvaluated}");
        }
        
        Debug.Log(sb.ToString());
    }
}