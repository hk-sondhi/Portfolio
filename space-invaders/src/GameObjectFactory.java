import java.awt.*;
import java.util.ArrayList;
import java.awt.event.KeyEvent;
import javax.swing.*;
import java.util.Random;

public class GameObjectFactory {

    // Creates a single Alien object
    public static Alien createAlien(int x, int y, int speed, String imagePath) {
        return new Alien(x, y, speed, imagePath);
    }

    // Creates a grid of Alien objects
    public static ArrayList<Alien> createAlienGrid(int rows, int columns, int alienWidth, int alienHeight, int horizontalSpacing, int verticalSpacing) {
        ArrayList<Alien> aliens = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int x = 50 + col * (alienWidth + horizontalSpacing); // Calculate X position
                int y = 50 + row * (alienHeight + verticalSpacing); // Calculate Y position

                // Use a different image for the top row
                String imagePath = (row == 0) ? "/Images/alienshoot.jpg" : "/Images/hqdefault.jpg";
                aliens.add(createAlien(x, y, 2, imagePath));
            }
        }

        return aliens;
    }

    // Creates a special Alien object
    public static Alien createSpecialAlien() {
        return createAlien(-40, 20, 4, "/Images/onealien.jpg"); // Special alien starts off-screen
    }

    // Creates a list of Shield objects positioned near the player
    public static ArrayList<Shield> createShields(Player player, int count, int width, int height, int spacing, String imagePath) {
        ArrayList<Shield> shields = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int x = 50 + i * spacing; // Calculate X position
            int y = player.getPositionY() - 60; // Position above the player
            shields.add(new Shield(x, y, imagePath, width, height));
        }
        return shields;
    }

    // Creates a Bullet object
    public static Bullet createBullet(int x, int y, int speed, String owner) {
        return new Bullet(x, y, speed, owner);
    }

    // Creates a CollisionDetector with a chain of responsibility for collision handling
    public static CollisionDetector createCollisionDetector(ScoreManager scoreManager, LivesManager livesManager) {
        // Create the chain of responsibility for collision detection
        CollisionHandler playerHandler = new PlayerCollisionHandler();
        CollisionHandler alienHandler = new AlienCollisionHandler();
        CollisionHandler shieldHandler = new ShieldCollisionHandler();
        CollisionHandler specialAlienHandler = new SpecialAlienCollisionHandler();

        // Link the handlers
        playerHandler.setNextHandler(alienHandler);
        alienHandler.setNextHandler(shieldHandler);
        shieldHandler.setNextHandler(specialAlienHandler);

        // Create and return the CollisionDetector
        return new CollisionDetector(scoreManager, livesManager, playerHandler); // Start of the chain
    }

    // Creates a Player object
    public static Player createPlayer() {
        return new Player(); // Uses the Player class's default constructor
    }

    // Creates a ScoreManager object
    public static ScoreManager createScoreManager() {
        return new ScoreManager(); // Uses the ScoreManager class's constructor
    }

    // Creates a LivesManager with the specified initial lives
    public static LivesManager createLivesManager(int initialLives) {
        return new LivesManager(initialLives);
    }

    // Creates an InputHandler to handle player inputs
    public static InputHandler createInputHandler(Player player, ArrayList<Bullet> bullets) {
        return new InputHandler(player, bullets);
    }

    // Creates the main game board
    public static GameBoard createGameBoard() {
        return new GameBoard();
    }

    // Makes an alien shoot a bullet
    public static void alienShoot(Alien alien, ArrayList<Bullet> bullets) {
        bullets.add(createBullet(alien.getPositionX() + 20, alien.getPositionY() + 20, 3, "alien"));
    }

    // Makes the player shoot a bullet
    public static void playerShoot(Player player, ArrayList<Bullet> bullets) {
        if (System.currentTimeMillis() - player.getLastShotTime() >= 500) { // Enforce shooting cooldown
            bullets.add(createBullet(player.getPositionX() + 20, player.getPositionY(), -10, "player"));
            player.updateLastShotTime(); // Update the player's last shot time
        }
    }

    // Interface for defining key actions
    public interface KeyAction {
        void performAction();
    }

    // Maps key codes to specific player actions
    public static KeyAction getKeyAction(int keyCode, Player player, ArrayList<Bullet> bullets) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                return player::moveLeft; // Move the player left
            case KeyEvent.VK_RIGHT:
                return player::moveRight; // Move the player right
            case KeyEvent.VK_SPACE:
                return () -> player.shoot(bullets); // Shoot a bullet
            default:
                return () -> {
                    // No action for unspecified keys
                };
        }
    }

    // Initialises the game state
    public static GameEngine.GameState initializeGameState(GameEngine.GameState state) {
        return state;
    }

    // Creates and sets up the main game window
    public static JFrame createGameFrame(String title, GameBoard gameBoard, InputHandler inputHandler) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the application on exit
        frame.setResizable(false);
        frame.add(gameBoard); // Add the game board to the frame
        frame.addKeyListener(inputHandler); // Attach the input handler
        frame.pack(); // Adjust the frame size
        frame.setVisible(true); // Make the frame visible
        return frame;
    }

    // Creates a random special alien with a random speed
    public static Alien createRandomSpecialAlien() {
        Random random = new Random();
        int speed = random.nextInt(3) + 2;
        return createAlien(-40, 20, speed, "/Images/onealien.jpg"); // Special alien starts off-screen
    }

    // Returns the current system time
    public static long createTimer() {
        return System.currentTimeMillis();
    }

    // Makes a random alien from the top row shoot
    public static void oneAlienShoot(ArrayList<Alien> topRowAliens, ArrayList<Bullet> bullets) {
        if (!topRowAliens.isEmpty()) {
            int randomIndex = new Random().nextInt(topRowAliens.size()); // Choose a random alien
            Alien shootingAlien = topRowAliens.get(randomIndex);
            shootingAlien.shoot(bullets); // Make the alien shoot
        }
    }

    // Formats the score for display
    public static String formatScore(int score) {
        return "Score: " + score;
    }

    // Creates the life icon image
    public static Image createLifeIcon(String imagePath) {
        try {
            return new ImageIcon(GameObjectFactory.class.getResource(imagePath)).getImage(); // Load the image
        } catch (Exception e) {
            System.err.println("Life icon image not found at " + imagePath); // Log error if the image is missing
            return null;
        }
    }

    // Creates a chain of ScoreEventHandlers
    public static ScoreEventHandler createScoreEventHandlerChain() {
        ScoreEventHandler alienHitHandler = new AlienHitHandler(); // Handle regular alien hits
        ScoreEventHandler specialAlienHitHandler = new SpecialAlienHitHandler(); // Handle special alien hits

        alienHitHandler.setNextHandler(specialAlienHitHandler); // Link the handlers

        return alienHitHandler; // Return the start of the chain
    }
}
