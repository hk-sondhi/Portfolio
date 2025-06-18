import java.util.ArrayList;

//detects collisons between objects and delegates appropriate actions to handlers
public class CollisionDetector {
    private final ScoreManager scoreManager;    // Manages and updates the player's score during the game.
    private final LivesManager livesManager;    //Manages the player's lives
    private final CollisionHandler collisionChain;  //handles collisions
    private final ScoreEventHandler scoreEventHandler; // handles score events

    private ArrayList<Bullet> bullets; // list of all bullets

    //constructors
    public CollisionDetector(ScoreManager scoreManager, LivesManager livesManager, CollisionHandler collisionChain) {
        this.scoreManager = scoreManager;
        this.livesManager = livesManager;
        this.collisionChain = collisionChain;
        this.scoreEventHandler = GameObjectFactory.createScoreEventHandlerChain(); // Initialize the score chain
        this.bullets = new ArrayList<>(); // Initialize with an empty list
    }

    //Detects collisions between bullets and other game objects
    public void detectCollisions(Player player, ArrayList<Alien> aliens, ArrayList<Bullet> bullets, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive) {
        this.bullets = bullets; // Update the bullets list
        for (Bullet bullet : new ArrayList<>(bullets)) { // Avoid concurrent modification
            collisionChain.handleCollision(bullet, player, aliens, shields, specialAlien, specialAlienActive, this);
        }
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public LivesManager getLivesManager() {
        return livesManager;
    }

    public void setSpecialAlienActive(boolean active) {
        // Provide setter to update specialAlienActive status
    }

    public ArrayList<Bullet> getBullets() {
        return bullets; // Return the updated bullets list
    }

}