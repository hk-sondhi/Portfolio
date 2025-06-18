//The ScoreManager class manages the player's score in the game,
// by tracking, updating, and resetting the score.
public class ScoreManager {
    // The current score in the game
    private int score;

    //Constructor to initialise the ScoreManager, where initial score is set to 0
    public ScoreManager() {
        this.score = 0;
    }

    // returns current score as integer
    public int getScore() {
        return score;
    }

    // increments score by given points
    public void addScore(int points) {
        score += points;
    }

    // resets score to 0
    public void reset() {
        score = 0;
    }
}
