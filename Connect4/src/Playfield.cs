//Script influenced and adapted from tutorial by octoman 2021 and Blue Buffalo 2021
//Udemy. (n.d.) Connect 4 Game Programming for Unity 3D. Udemy [Online]. Available at: https://www.udemy.com/course/connect-4-game-programming-course-for-unity-3d/ (Accessed: 10 Nov 2024).
//Blue Buffalo. (2021) Connect 4 Unity Tutorial - Programs and Overview. YouTube video. Available at: https://www.youtube.com/watch?v=t137iSk-JUg (Accessed: 7 Nov. 2025).
// The code has been adapted and expanded to fit the specific requirements of this project.

using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using UnityEngine;
using System.Text;

// Manages the Connect Four game board, valid moves, and win conditions
public class Playfield : MonoBehaviour
{
    #region Singleton
    public static Playfield instance;
    #endregion

    #region Constants
    // Board dimensions
    private static readonly int Height = 6;
    private static readonly int Width = 7;
    #endregion

    #region Player Type Enum
    public enum PlayerType
    {
        Empty = 0,
        PlayerOne = 1,
        PlayerTwo = 2
    }
    #endregion

    #region Game State
    // Board state encapsulation
    [Serializable]
    private class BoardState
    {
        private PlayerType[,] grid;
        
        public BoardState()
        {
            grid = new PlayerType[Height, Width];
            // Initialise with Empty values
            Reset();
        }
        
        public PlayerType GetCell(int row, int column)
        {
            if (IsValidPosition(row, column))
                return grid[row, column];
            
            return PlayerType.Empty; // Default for invalid positions
        }
        
        public void SetCell(int row, int column, PlayerType value)
        {
            if (IsValidPosition(row, column))
                grid[row, column] = value;
        }
        
        public bool IsValidPosition(int row, int column)
        {
            return row >= 0 && row < Height && column >= 0 && column < Width;
        }
        
        public bool IsFull()
        {
            for (int row = 0; row < Height; row++)
            {
                for (int column = 0; column < Width; column++)
                {
                    if (grid[row, column] == PlayerType.Empty)
                        return false;
                }
            }
            return true;
        }
        
        public void Reset()
        {
            for (int row = 0; row < Height; row++)
            {
                for (int column = 0; column < Width; column++)
                {
                    grid[row, column] = PlayerType.Empty;
                }
            }
        }
        
        public BoardState Clone()
        {
            BoardState clone = new BoardState();
            for (int row = 0; row < Height; row++)
            {
                for (int column = 0; column < Width; column++)
                {
                    clone.grid[row, column] = grid[row, column];
                }
            }
            return clone;
        }
        
        public int[,] ToIntArray()
        {
            int[,] result = new int[Height, Width];
            for (int row = 0; row < Height; row++)
            {
                for (int column = 0; column < Width; column++)
                {
                    result[row, column] = (int)grid[row, column];
                }
            }
            return result;
        }
    }

    // Main board state
    private BoardState board = new BoardState();
    
    // Cache for win checking to avoid redundant calculations
    private struct WinCheckCache
    {
        public bool[,] checkedHorizontal;
        public bool[,] checkedVertical;
        public bool[,] checkedDiagonalUp;
        public bool[,] checkedDiagonalDown;
        
        public void Reset()
        {
            checkedHorizontal = new bool[Height, Width];
            checkedVertical = new bool[Height, Width];
            checkedDiagonalUp = new bool[Height, Width];
            checkedDiagonalDown = new bool[Height, Width];
        }
    }
    
    private WinCheckCache winCheckCache;

    // Optional settings
    [SerializeField, Tooltip("Enable to show debug board state in console")]
    private bool showDebugBoard = false;
    #endregion

    #region Unity Lifecycle Methods
    
    // Initialise singleton instance
    void Awake()
    {
        // Implement proper singleton pattern
        if (instance != null && instance != this)
        {
            Debug.LogWarning("Multiple Playfield instances detected. Destroying duplicate.");
            Destroy(gameObject);
            return;
        }
        
        instance = this;
        
        // Initialise the win check cache
        winCheckCache.Reset();
    }
    #endregion

    #region Game Board Methods
    
    // Checks if a move is valid and returns the row where the coin would land
    public int ValidMove(int column)
    {
        // Validate column is within board boundaries
        if (column < 0 || column >= Width)
        {
            Debug.LogWarning($"Playfield: Column {column} is out of bounds!");
            return -1;
        }

        // Check from bottom to top for the first empty cell
        for (int row = Height - 1; row >= 0; row--)
        {
            if (board.GetCell(row, column) == PlayerType.Empty)
            {
                return row;
            }
        }
        
        // Column is full
        if (showDebugBoard)
        {
            Debug.Log("No Valid Move");
        }
        
        return -1;
    }

    
    // Places a coin on the board and checks win conditions
    public void DropCoin(int row, int column, int playerValue)
    {
        PlayerType player = (PlayerType)playerValue;
        
        // Validate parameters
        if (row < 0 || row >= Height || column < 0 || column >= Width)
        {
            Debug.LogError($"Playfield: Invalid position ({row}, {column})");
            return;
        }
        
        if (player != PlayerType.PlayerOne && player != PlayerType.PlayerTwo)
        {
            Debug.LogError($"Playfield: Invalid player value: {playerValue}");
            return;
        }
        
        // Place the coin
        board.SetCell(row, column, player);
        
        // Reset win checking cache since board has changed
        winCheckCache.Reset();
        
        // Debug output if enabled
        if (showDebugBoard)
        {
            Debug.Log("Current Board \n" + DebugBoard());
        }
        
        // Check win conditions
        GameManager.instance.WinCondition(WinCheck());
    }

    
    // Checks if the game has a winner or is a draw
    bool WinCheck()
    {
        // Check for win in any direction
        if (HorizontalCheck() || VerticalCheck() || DiagonalCheck())
        {
            if (InfoBox.instance != null)
            {
                InfoBox.instance.ShowMessage("Game Over!");
            }
            return true;
        }

        // Check for draw
        if (IsBoardFull())
        {
            if (GameManager.instance != null)
            {
                GameManager.instance.DrawCondition();
            }
            
            if (InfoBox.instance != null)
            {
                InfoBox.instance.ShowMessage("Game Over! It's a draw!");
            }
            return true;
        }

        return false;
    }

    
    // Checks if the board is completely full (draw condition)
    bool IsBoardFull()
    {
        return board.IsFull();
    }
    #endregion

    #region Win Condition Checks
    
    // Checks for horizontal wins (4 in a row)
    bool HorizontalCheck()
    {
        for (int row = 0; row < Height; row++)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                // Skip if already checked this position or it's empty
                if (winCheckCache.checkedHorizontal[row, column] || 
                    board.GetCell(row, column) == PlayerType.Empty)
                {
                    continue;
                }
                
                // Mark this position as checked
                winCheckCache.checkedHorizontal[row, column] = true;

                PlayerType a = board.GetCell(row, column);
                PlayerType b = board.GetCell(row, column + 1);
                PlayerType c = board.GetCell(row, column + 2);
                PlayerType d = board.GetCell(row, column + 3);
                
                if (a == b && a == c && a == d)
                {
                    // Highlight winning combo
                    if (GameManager.instance != null)
                    {
                        GameManager.instance.ShowWinCircles(
                            new Vector2(row, column),
                            new Vector2(row, column + 1),
                            new Vector2(row, column + 2),
                            new Vector2(row, column + 3)
                        );
                    }
                    
                    if (showDebugBoard)
                    {
                        Debug.Log($"We have a winner: {a}");
                    }
                    return true;
                }
            }
        }
        
        if (showDebugBoard)
        {
            Debug.Log("No Winner Yet");
        }
        return false;
    }
    
    
    // Checks for vertical wins (4 in a column)
    bool VerticalCheck()
    {
        for (int row = 0; row <= Height - 4; row++)
        {
            for (int column = 0; column < Width; column++)
            {
                // Skip if already checked this position or it's empty
                if (winCheckCache.checkedVertical[row, column] || 
                    board.GetCell(row, column) == PlayerType.Empty)
                {
                    continue;
                }
                
                // Mark this position as checked
                winCheckCache.checkedVertical[row, column] = true;

                PlayerType a = board.GetCell(row, column);
                PlayerType b = board.GetCell(row + 1, column);
                PlayerType c = board.GetCell(row + 2, column);
                PlayerType d = board.GetCell(row + 3, column);

                if (a == b && a == c && a == d)
                {
                    // Highlight winning combo
                    if (GameManager.instance != null)
                    {
                        GameManager.instance.ShowWinCircles(
                            new Vector2(row, column),
                            new Vector2(row + 1, column),
                            new Vector2(row + 2, column),
                            new Vector2(row + 3, column)
                        );
                    }
                    
                    if (showDebugBoard)
                    {
                        Debug.Log($"We have a winner: {a}");
                    }
                    return true;
                }
            }
        }
        
        if (showDebugBoard)
        {
            Debug.Log("No Winner Yet");
        }
        return false;
    }

    
    // Checks for diagonal wins (4 in a diagonal)
    bool DiagonalCheck()
    {
        // Check for diagonal wins (bottom-left to top-right)
        if (CheckDiagonalBottomLeftToTopRight())
        {
            return true;
        }
        
        // Check for diagonal wins (top-left to bottom-right)
        if (CheckDiagonalTopLeftToBottomRight())
        {
            return true;
        }
        
        if (showDebugBoard)
        {
            Debug.Log("No Winner Yet");
        }
        
        return false;
    } 

    
    // Checks for diagonal wins from bottom-left to top-right
    private bool CheckDiagonalBottomLeftToTopRight()
    {
        for (int row = Height - 1; row >= 3; row--)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                // Skip if already checked this position or it's empty
                if (winCheckCache.checkedDiagonalUp[row, column] || 
                    board.GetCell(row, column) == PlayerType.Empty)
                {
                    continue;
                }
                
                // Mark this position as checked
                winCheckCache.checkedDiagonalUp[row, column] = true;
                
                PlayerType a = board.GetCell(row, column);
                PlayerType b = board.GetCell(row - 1, column + 1);
                PlayerType c = board.GetCell(row - 2, column + 2);
                PlayerType d = board.GetCell(row - 3, column + 3);
                
                if (a == b && a == c && a == d)
                {
                    // Highlight winning combo
                    if (GameManager.instance != null)
                    {
                        GameManager.instance.ShowWinCircles(
                            new Vector2(row, column),
                            new Vector2(row - 1, column + 1),
                            new Vector2(row - 2, column + 2),
                            new Vector2(row - 3, column + 3)
                        );
                    }
                    
                    if (showDebugBoard)
                    {
                        Debug.Log($"We have a winner: {a}");
                    }
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    // Checks for diagonal wins from top-left to bottom-right
    private bool CheckDiagonalTopLeftToBottomRight()
    {
        for (int row = 0; row <= Height - 4; row++)
        {
            for (int column = 0; column <= Width - 4; column++)
            {
                // Skip if already checked this position or it's empty
                if (winCheckCache.checkedDiagonalDown[row, column] || 
                    board.GetCell(row, column) == PlayerType.Empty)
                {
                    continue;
                }
                
                // Mark this position as checked
                winCheckCache.checkedDiagonalDown[row, column] = true;
                
                PlayerType a = board.GetCell(row, column);
                PlayerType b = board.GetCell(row + 1, column + 1);
                PlayerType c = board.GetCell(row + 2, column + 2);
                PlayerType d = board.GetCell(row + 3, column + 3);
                
                if (a == b && a == c && a == d)
                {
                    // Highlight winning combo
                    if (GameManager.instance != null)
                    {
                        GameManager.instance.ShowWinCircles(
                            new Vector2(row, column),
                            new Vector2(row + 1, column + 1),
                            new Vector2(row + 2, column + 2),
                            new Vector2(row + 3, column + 3)
                        );
                    }
                    
                    if (showDebugBoard)
                    {
                        Debug.Log($"We have a winner: {a}");
                    }
                    return true;
                }
            }
        }
        
        return false;
    }
    #endregion
    #region Utility Methods
    
    // Creates a debug string representation of the board
    string DebugBoard()
    {
        StringBuilder sb = new StringBuilder();
        string separator = ",";
        string border = "|";

        for (int x = 0; x < Height; x++)
        {
            sb.Append(border);
            for (int y = 0; y < Width; y++)
            {
                sb.Append((int)board.GetCell(x, y));
                sb.Append(separator);
            }
            sb.Append(border);
            sb.Append('\n');
        }
        return sb.ToString();
    }

    
    // Returns a copy of the current board for AI calculations 
    public int[,] CurrentPlayfield()
    {
        return board.ToIntArray();
    }
    
    
    // Resets the board to an empty state
    public void ResetBoard()
    {
        // Reset the entire board to empty
        board.Reset();
        
        // Reset win checking cache
        winCheckCache.Reset();
        
        if (showDebugBoard)
        {
            Debug.Log("Board has been reset");
            Debug.Log(DebugBoard());
        }
    }
    
    
    // Gets the current value at a board position
    public int GetBoardValue(int row, int column)
    {
        if (row < 0 || row >= Height || column < 0 || column >= Width)
        {
            return -1; // Invalid position
        }
        
        return (int)board.GetCell(row, column);
    }
    #endregion
}