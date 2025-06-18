import java.util.Random;
import java.util.ArrayList;
import javax.swing.*;

public class GameEngine {
    private Player player; // The player object
    private ArrayList<Alien> aliens; // List of all active aliens
    private Alien specialAlien; // Special alien object
    private boolean specialAlienActive; // Tracks if the special alien is currently active
    private Random random; // Random number generator
    private ArrayList<Bullet> bullets; // List of active bullets
    private CollisionDetector collisionDetector; // Handles collision detection
    private ScoreManager scoreManager; // Manages the player's score
    private LivesManager livesManager; // Manages the player's lives
    private GameBoard gameBoard; // The visual representation of the game
    private JFrame frame; // The main game window

    private long lastAlienBulletTime; // Tracks the time of the last alien bullet
    private final int alienBulletCooldown = 4000; // Cooldown for alien bullets (milliseconds)

    private long specialAlienAppearanceTime; // Tracks the last appearance of the special alien
    private final int specialAlienCooldown = 15000; // Minimum interval between special alien appearances (milliseconds)

    private int score; // Tracks the player's score
    private ArrayList<Shield> shields; // List of active shields

    public enum GameState {START, PLAYING, PAUSED, GAME_OVER}
    private GameState gameState; // Current state of the game

    // Constructor
    public GameEngine() {
        // Initialise game objects and managers
        player = GameObjectFactory.createPlayer();
        bullets = new ArrayList<>();
        scoreManager = GameObjectFactory.createScoreManager();
        livesManager = GameObjectFactory.createLivesManager(3); // Start with 3 lives

        // Set up the collision handling chain
        CollisionHandler playerHandler = new PlayerCollisionHandler();
        CollisionHandler alienHandler = new AlienCollisionHandler();
        CollisionHandler shieldHandler = new ShieldCollisionHandler();
        CollisionHandler specialAlienHandler = new SpecialAlienCollisionHandler();

        // Link the handlers in a chain
        playerHandler.setNextHandler(alienHandler);
        alienHandler.setNextHandler(shieldHandler);
        shieldHandler.setNextHandler(specialAlienHandler);

        // Initialise the collision detector
        collisionDetector = new CollisionDetector(scoreManager, livesManager, playerHandler);

        // Set up the game board
        gameBoard = GameObjectFactory.createGameBoard();

        // Initialise random number generator
        random = new Random();

        // Create aliens and shields
        aliens = GameObjectFactory.createAlienGrid(5, 10, 40, 20, 20, 30); // Alien grid: 5 rows, 10 columns
        specialAlien = GameObjectFactory.createSpecialAlien(); //calls factory class
        specialAlienActive = false; // Initially inactive

        shields = GameObjectFactory.createShields(player, 4, 80, 40, 200, "/Images/shield.jpeg"); // 4 shields

        // Initialise score
        score = 0;

        // Set up input handling
        InputHandler inputHandler = GameObjectFactory.createInputHandler(player, bullets);

        // Create and configure the game window
        frame = GameObjectFactory.createGameFrame("Space Invaders", gameBoard, inputHandler);

        // Initialise timers using factory class
        specialAlienAppearanceTime = GameObjectFactory.createTimer(); // Start timer for special alien
        gameState = GameObjectFactory.initializeGameState(GameState.START); // Set initial game state
    }

    // Starts the game loop
    public void startGame() {
        gameState = GameObjectFactory.initializeGameState(GameState.PLAYING);
        runGameLoop();
    }

    // Main game loop
    private void runGameLoop() {
        while (gameState == GameState.PLAYING) {
            // Check if the game is over
            if (livesManager.isGameOver()) {
                gameState = GameObjectFactory.initializeGameState(GameState.GAME_OVER);
                showGameOverScreen(); // Display the game over screen
                System.out.println("Game Over! You lost all lives.");
                break;
            }

            // Update game state
            updateObjects();
            checkCollisions();
            renderGame();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Handle game over state
        if (gameState == GameState.GAME_OVER) {
            showGameOverScreen();
        }
    }

    // Returns the game board
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    // Displays the game over screen
    private void showGameOverScreen() {
        frame.getContentPane().removeAll(); // Clear the frame
        GameOverScreen gameOverScreen = new GameOverScreen(frame, this, scoreManager.getScore());
        frame.add(gameOverScreen); // Add the Game Over screen
        frame.revalidate();
        frame.repaint();
    }

    // Updates game objects
    private void updateObjects() {
        player.update(); // Update player position

        // Update bullet positions
        for (Bullet bullet : bullets) {
            bullet.update();
        }

        // Move aliens and handle edge collisions
        boolean hitEdge = false;
        int dropDistance = 3; // Distance to drop when aliens hit the edge

        for (Alien alien : aliens) {
            alien.setPositionX(alien.getPositionX() + alien.getSpeed());
            if (alien.getPositionX() <= 0 || alien.getPositionX() >= 760) {
                hitEdge = true; // Check if any alien hits the edge
            }
            if (alien.getPositionY() > player.getPositionY()) {
                gameState = GameObjectFactory.initializeGameState(GameState.GAME_OVER); // End game if aliens pass
                System.out.println("Game Over! An alien passed your defenses.");
                return;
            }
        }

        // Reverse direction and move aliens down if they hit the edge
        if (hitEdge) {
            for (Alien alien : aliens) {
                alien.setSpeed(-alien.getSpeed());
                alien.setPositionY(alien.getPositionY() + dropDistance);
            }
        }

        handleSpecialAlien(); // Manage special alien behaviour

        // Allow aliens to shoot based on cooldown
        long currentTime = GameObjectFactory.createTimer();
        if (currentTime - lastAlienBulletTime >= alienBulletCooldown) {
            ArrayList<Alien> topRowAliens = getTopRowAliens();
            GameObjectFactory.oneAlienShoot(topRowAliens, bullets); // Random alien shoots
            lastAlienBulletTime = currentTime; // Update timer
        }
    }

    // Manages special alien
    private void handleSpecialAlien() {
        long currentTime = GameObjectFactory.createTimer();

        if (!specialAlienActive && currentTime - specialAlienAppearanceTime >= specialAlienCooldown) {
            specialAlien = GameObjectFactory.createSpecialAlien(); // factory class to Spawn special alien
            specialAlien.setSpeed(new Random().nextInt(3) + 2);
            specialAlienActive = true;
            specialAlienAppearanceTime = GameObjectFactory.createTimer(); // Reset timer
        }

        if (specialAlienActive) {
            specialAlien.setPositionX(specialAlien.getPositionX() + specialAlien.getSpeed());
            if (specialAlien.getPositionX() > 800) {
                specialAlienActive = false; // Deactivate special alien when it moves off-screen
                specialAlienAppearanceTime = GameObjectFactory.createTimer(); // Reset timer
            }
        }
    }

    // Checks for collisions
    private void checkCollisions() {
        collisionDetector.detectCollisions(player, aliens, bullets, shields, specialAlien, specialAlienActive);
    }

    // Renders the game objects
    private void renderGame() {
        gameBoard.render(player, aliens, bullets, scoreManager.getScore(), livesManager.getLives(), specialAlien, specialAlienActive, shields);
    }

    // Gets the top row of aliens
    private ArrayList<Alien> getTopRowAliens() {
        ArrayList<Alien> topRow = new ArrayList<>();
        for (Alien alien : aliens) {
            boolean isTopRow = true;
            for (Alien otherAlien : aliens) {
                if (otherAlien.getPositionY() < alien.getPositionY() &&
                        otherAlien.getPositionX() == alien.getPositionX()) {
                    isTopRow = false;
                    break;
                }
            }
            if (isTopRow) {
                topRow.add(alien);
            }
        }
        return topRow;
    }

    // Resets the game state
    public void reset() {
        player = GameObjectFactory.createPlayer(); // Reset player
        aliens = GameObjectFactory.createAlienGrid(5, 10, 40, 20, 20, 30); // Reset aliens
        specialAlien = GameObjectFactory.createSpecialAlien();
        specialAlienActive = false;

        shields = GameObjectFactory.createShields(player, 4, 80, 40, 200, "/Images/shield.jpeg"); // Reset shields

        bullets.clear(); // Clear bullets
        scoreManager.reset(); // Reset score
        livesManager = GameObjectFactory.createLivesManager(3); // Reset lives

        lastAlienBulletTime = 0; // Reset timers
        specialAlienAppearanceTime = GameObjectFactory.createTimer();

        gameState = GameObjectFactory.initializeGameState(GameState.PLAYING); // Reset game state

        frame.getContentPane().removeAll(); // Repaint game board
        frame.add(gameBoard);
        frame.revalidate();
        frame.repaint();
    }
}
