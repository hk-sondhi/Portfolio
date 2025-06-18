//Script influenced and adapted from tutorial by octoman 2021 and Blue Buffalo 2021
//Udemy. (n.d.) Connect 4 Game Programming for Unity 3D. Udemy [Online]. Available at: https://www.udemy.com/course/connect-4-game-programming-course-for-unity-3d/ (Accessed: 10 Nov 2024).
//Blue Buffalo. (2021) Connect 4 Unity Tutorial - Programs and Overview. YouTube video. Available at: https://www.youtube.com/watch?v=t137iSk-JUg (Accessed: 7 Nov. 2025).

using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UIElements;

public class ButtonHover : MonoBehaviour
{
    public GameObject objToShow;

    public int column;
    
    void Start()
    {
        objToShow.SetActive(false);
    }

    public void OnMouseEnter()
    {
        objToShow.SetActive(true);
    }

    public void OnMouseExit()
    {
        objToShow.SetActive(false);
    }

}
