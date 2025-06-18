//Script influenced and adapted from tutorial by octoman 2021 and Blue Buffalo 2021
//Udemy. (n.d.) Connect 4 Game Programming for Unity 3D. Udemy [Online]. Available at: https://www.udemy.com/course/connect-4-game-programming-course-for-unity-3d/ (Accessed: 10 Nov 2024).
//Blue Buffalo. (2021) Connect 4 Unity Tutorial - Programs and Overview. YouTube video. Available at: https://www.youtube.com/watch?v=t137iSk-JUg (Accessed: 7 Nov. 2025).
// The code has been adapted and expanded to fit the specific requirements of this project.

using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ColumnInput : MonoBehaviour
{
    public int column;



    void OnMouseOver()
    {
        if(Input.GetMouseButtonDown(0))
        {
            Debug.Log("Column Pressed: " + column);
            GameManager.instance.ColumnPressed(column);
        }
    }
}
