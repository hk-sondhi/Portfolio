import java.util.ArrayList;

// handles collisions between bullets and shields, following the Chain of Responsibility pattern.
public class ShieldCollisionHandler implements CollisionHandler {
    private CollisionHandler nextHandler;  // Reference to the next handler in the chain (if any)

    //handles collisions between a bullet and any shield in the game.
    @Override
    public void handleCollision(Bullet bullet, Player player, ArrayList<Alien> aliens, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive, CollisionDetector detector) {
        // Loop through each shield to check if the bullet collides with it.
        for (Shield shield : shields) {
            // Check if the shield is not destroyed and if the bullet is within the shield's bounds.
            if (!shield.isDestroyed() &&
                    bullet.getPositionX() >= shield.getPositionX() &&  // Bullet's X position must be within shield's X range
                    bullet.getPositionX() < shield.getPositionX() + shield.getWidth() &&  // Bullet's X position must be within shield's width
                    bullet.getPositionY() >= shield.getPositionY() &&  // Bullet's Y position must be within shield's Y range
                    bullet.getPositionY() < shield.getPositionY() + shield.getHeight()) {  // Bullet's Y position must be within shield's height
                // If the bullet collides with the shield, the shield takes damage
                shield.takeDamage(bullet.getPositionX(), bullet.getPositionY());
                // Remove the bullet from the list of active bullets
                detector.getBullets().remove(bullet);
                return;  // Exit the method after handling the collision.
            }
        }

        // If the bullet didn't hit any shields, pass the responsibility to the next handler in the chain
        if (nextHandler != null) {
            nextHandler.handleCollision(bullet, player, aliens, shields, specialAlien, specialAlienActive, detector);
        }
    }

    // This method sets the next handler in the chain of responsibility.
    @Override
    public void setNextHandler(CollisionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
