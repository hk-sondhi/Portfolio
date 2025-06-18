// The LivesManager class is responsible for managing the player's lives in the game
//  By tracking, decreasing, and resetting lives, as well as determining if the game is over.
public class LivesManager {
    //number of lives remaining in game
    private int lives;

    //Constructor to initialise the LivesManager with a specified number of initial lives.
    public LivesManager(int initialLives) {
        this.lives = initialLives;
    }

    //returns the number of lives remaining
    public int getLives() {
        return lives;
    }

    //Decreases the number of lives by one, ensuring the value does not go below zero.
    // If the lives reach zero, it prints a "Game Over!" message.
    public void decreaseLives() {
        if (lives > 0) {
            lives--;
        }

        if (lives <= 0) {
            System.out.println("Game Over!");
        }
    }

    // checks if the game is over based on remaining lives and
    // returns true is lives are 0 or less, indicating game is over
    public boolean isGameOver() {
        return lives <= 0;
    }

}
