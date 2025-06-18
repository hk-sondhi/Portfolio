//Script influenced by tutorial by Daniel Wood (2021)
// Daniel Wood (2021). How To Create A Scrolling Background In Unity - EASY TUTORIAL. YouTube. Available at: https://www.youtube.com/watch?v=Wz3nbQPYwss [Accessed 27 Feb. 2025]

using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ScrollingBehaviour : MonoBehaviour
{
    public float speed;
    [SerializeField]
    private Renderer bgRenderer;

    // Update is called once per frame
    void Update()
    {
        bgRenderer.material.mainTextureOffset += new Vector2(speed*Time.deltaTime,0);
    }
}
