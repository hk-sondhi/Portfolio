import java.awt.Image;
import javax.swing.ImageIcon;
import java.util.ArrayList;

//for alien movement
interface MovementBehavior {
    void move(Alien alien);
}

//for alien shooting
interface ShootingBehavior {
    void shoot(Alien alien, ArrayList<Bullet> bullets);
}

// Default implementations of MovementBehavior and ShootingBehavior, where alien moves along horizontal axis,
// reversing direction when it hits the screen boundaries
class HorizontalMovement implements MovementBehavior {
    @Override
    public void move(Alien alien) {
        alien.setPositionX(alien.getPositionX() + alien.getSpeed());
        if (alien.getPositionX() <= 0 || alien.getPositionX() >= 760) { // Screen bounds
            alien.setSpeed(-alien.getSpeed());
        }
    }
}

//uses factory class to create bullets when the alien shoots
class DefaultShootingBehavior implements ShootingBehavior {
    @Override
    public void shoot(Alien alien, ArrayList<Bullet> bullets) {
        GameObjectFactory.alienShoot(alien, bullets);
    }
}

public class Alien {
    private int positionX; // alien's horizontal position
    private int positionY; // alien's vertical position
    private int speed;     // speed of the alien's movement
    private Image alienImage; // alien's image
    private MovementBehavior movementBehavior; // movement of alien
    private ShootingBehavior shootingBehavior; // alien shooting

    //constructor initialises alien object
    public Alien(int x, int y, int speed, String imagePath) {
        this(x, y, speed, imagePath, new HorizontalMovement(), new DefaultShootingBehavior());
    }

    public Alien(int x, int y, int speed, String imagePath, MovementBehavior movementBehavior, ShootingBehavior shootingBehavior) {
        this.positionX = x;
        this.positionY = y;
        this.speed = speed;
        this.movementBehavior = movementBehavior;
        this.shootingBehavior = shootingBehavior;
        this.alienImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
    }

    public void shoot(ArrayList<Bullet> bullets) {
        shootingBehavior.shoot(this, bullets);
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Image getAlienImage() {
        return alienImage;
    }
}
