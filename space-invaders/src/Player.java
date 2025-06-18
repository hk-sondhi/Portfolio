import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.ArrayList;

public class Player {
    private int positionX;
    private int positionY;
    private int speed;
    private int lives;
    private boolean isShooting;
    private long lastShotTime;
    private Image playerImage; // Image for the player

    public Player() {
        this.positionX = 100;
        this.positionY = 500;
        this.speed = 5;
        this.lives = 3;
        this.isShooting = false;

        // Load the player image
        playerImage = new ImageIcon(getClass().getResource("/Images/player.jpg")).getImage();
    }

    public void moveLeft() {
        if (positionX > 0) { // Prevent moving off the left edge
            positionX -= speed;
        }
    }

    public void moveRight() {
        if (positionX < 750) { // Prevent moving off the right edge (800 - player width)
            positionX += speed;
        }
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public Image getPlayerImage() {
        return playerImage; // Getter for the player image
    }

    public void shoot(ArrayList<Bullet> bullets) {
        GameObjectFactory.playerShoot(this, bullets);
    }

    public void update() {
        // Logic to handle player updates (e.g., movement)
    }

    public void decreaseLives() {
        lives--;
        if (lives <= 0) {
            System.out.println("Game Over!");
        }
    }

    public long getLastShotTime() {
        return lastShotTime;
    }

    public void updateLastShotTime() {
        lastShotTime = System.currentTimeMillis();
    }
}
