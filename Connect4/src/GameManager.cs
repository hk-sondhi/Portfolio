//Script influenced and adapted from tutorial by octoman 2021 and Blue Buffalo 2021
//Udemy. (n.d.) Connect 4 Game Programming for Unity 3D. Udemy [Online]. Available at: https://www.udemy.com/course/connect-4-game-programming-course-for-unity-3d/ (Accessed: 10 Nov 2024).
//Blue Buffalo. (2021) Connect 4 Unity Tutorial - Programs and Overview. YouTube video. Available at: https://www.youtube.com/watch?v=t137iSk-JUg (Accessed: 7 Nov. 2025).
// The code has been adapted and expanded to fit the specific requirements of this project.

using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;


// Manages the core game flow, player turns, and game state

public class GameManager : MonoBehaviour
{
    public static GameManager instance;

    // Player identifiers
    private const int PLAYER_ONE = 1;
    private const int PLAYER_TWO = 2;

    // Current player (1 = player, 2 = AI or player 2)
    private int currentPlayer = 1;

    // Controls input during animations or AI move
    private bool activeTurn = true;

    // Prefabs and UI references
    public GameObject redCoin;
    public GameObject yellowCoin;
    public Transform startPoint;
    public GameObject winCirclePrefab;
    public GameObject canvas;
    public GameObject gameOverWindow;

    void Awake()
    {
        instance = this; //singleton pattern for easy access
    }

    void Start()
    {
        gameOverWindow.SetActive(false);
        InfoBox.instance.ShowMessage("Player " + currentPlayer + "'s Turn!");
    }

    //called when a column is clicked or selected by AI
    public void ColumnPressed(int column)
    {
        if (!activeTurn)
        {
            Debug.Log("Wait until Turn is Over");
            return; //prevent spam input while a move is processing 
        }
        int y = Playfield.instance.ValidMove(column); //check if move is valid 
        if (y != -1)
        {
            StartCoroutine(PlayCoin(y, column)); //animate and play move 
        }
    }

    //handles coin animation and dropping 
    IEnumerator PlayCoin(int row, int column)
    {
        activeTurn = false; //disable input during animation 

        //instantiate the correct colored coin 
        GameObject coin = Instantiate((currentPlayer == 1 ? redCoin : yellowCoin)) as GameObject;
        coin.transform.position = new Vector3(startPoint.position.x + column,
                                                startPoint.position.y + 1,
                                                startPoint.position.z);

        Vector3 goalPos = new Vector3(startPoint.position.x + column,
                                                startPoint.position.y - row,
                                                startPoint.position.z);

        //move coin to its target row position 
        while (MoveToGoal(goalPos, coin)) { yield return null; }

        //update board and check win condition
        Playfield.instance.DropCoin(row, column, currentPlayer);
    }

    //moves coin towards goal with smoothing 
    //moves object gradually toward a target position 
    //moves small amount per frame, not all at once 
    bool MoveToGoal(Vector3 goalPos, GameObject coin)
    {
        // 5f * Time.deltaTime -> controls speed of movements - higher value = faster animation
        //coin.transform.position -> sets the coin's position to its new position, slightly closer to goalPos
        return goalPos != (coin.transform.position = Vector3.MoveTowards(coin.transform.position, goalPos, 5f * Time.deltaTime));
    }

    //called after coin is placed and win condition is checked 
    public void WinCondition(bool winner)
    {
        if (winner)
        {
            //could add sound effects 
        }
        else
        {
            activeTurn = true;
            //SWITCH THE PLAYER
            SwitchPlayer(); //no win, so move to next player's turn
        }
    }

    //switches current player and triggers AI 
    void SwitchPlayer()
    {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
        InfoBox.instance.ShowMessage("Player " + currentPlayer + "'s Turn!");

        //AI TURN
        if (Difficulty.difficulty != 0)
        {
            if (currentPlayer == 2)
            {
                AiMove.instance.BestMove();
            }
        }
    }

    //indicators for winning combos
    public void ShowWinCircles(Vector2 a, Vector2 b, Vector2 c, Vector2 d)
    {
        //A
        Vector3 posA = new Vector3(startPoint.position.x + a.y,
                                    startPoint.position.y - a.x,
                                    startPoint.position.z);
        GameObject circleA = Instantiate(winCirclePrefab, canvas.transform, false);
        circleA.transform.position = Camera.main.WorldToScreenPoint(posA);
        //B
        Vector3 posB = new Vector3(startPoint.position.x + b.y,
                                    startPoint.position.y - b.x,
                                    startPoint.position.z);
        GameObject circleB = Instantiate(winCirclePrefab, canvas.transform, false);
        circleB.transform.position = Camera.main.WorldToScreenPoint(posB);
        //C
        Vector3 posC = new Vector3(startPoint.position.x + c.y,
                                    startPoint.position.y - c.x,
                                    startPoint.position.z);
        GameObject circleC = Instantiate(winCirclePrefab, canvas.transform, false);
        circleC.transform.position = Camera.main.WorldToScreenPoint(posC);
        //D
        Vector3 posD = new Vector3(startPoint.position.x + d.y,
                                    startPoint.position.y - d.x,
                                    startPoint.position.z);
        GameObject circleD = Instantiate(winCirclePrefab, canvas.transform, false);
        circleD.transform.position = Camera.main.WorldToScreenPoint(posD);

        gameOverWindow.SetActive(true);
    }

    public void DrawCondition()
    {
        activeTurn = false;
        gameOverWindow.SetActive(true);
        InfoBox.instance.ShowMessage("It's a draw!");
    }
}