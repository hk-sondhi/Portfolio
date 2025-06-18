import java.util.ArrayList;

public class SpecialAlienCollisionHandler implements CollisionHandler {
    private CollisionHandler nextHandler; // Reference to the next handler in the chain

    // handles the collision between the bullet and the special alien
    @Override
    public void handleCollision(Bullet bullet, Player player, ArrayList<Alien> aliens, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive, CollisionDetector detector) {
        // Check if special alien is active and bullet belongs to the player
        if (specialAlienActive && bullet.getOwner().equals("player") &&
                bullet.getPositionX() < specialAlien.getPositionX() + 40 &&  // Check if bullet is within the X bounds of the special alien
                bullet.getPositionX() + 5 > specialAlien.getPositionX() &&
                bullet.getPositionY() < specialAlien.getPositionY() + 20 &&  // Check if bullet is within the Y bounds of the special alien
                bullet.getPositionY() + 10 > specialAlien.getPositionY()) {

            // If collision detected, print message, remove bullet, add score, and deactivate special alien
            System.out.println("Special alien hit!");
            detector.getBullets().remove(bullet);  // Remove the bullet from the game
            detector.getScoreManager().addScore(50);  // Add score for hitting the special alien
            detector.setSpecialAlienActive(false);  // Deactivate special alien
        }
        // If collision is not handled, pass the responsibility to the next handler in the chain
        else if (nextHandler != null) {
            nextHandler.handleCollision(bullet, player, aliens, shields, specialAlien, specialAlienActive, detector);
        }
    }

    // Set the next handler in the chain of responsibility
    @Override
    public void setNextHandler(CollisionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
