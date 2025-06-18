import java.util.ArrayList;

// Handles collisions involving the player and bullets
// If the bullet shot by an alien and intersects with the player, players live decreased
// otherwise it passes the collision handling to the next handler in the chain
public class PlayerCollisionHandler implements CollisionHandler {
    private CollisionHandler nextHandler;

    @Override
    public void handleCollision(Bullet bullet, Player player, ArrayList<Alien> aliens, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive, CollisionDetector detector) {
        // Check if the bullet is shot by an alien and collides with a player
        if (bullet.getOwner().equals("alien") &&
                bullet.getPositionX() < player.getPositionX() + 50 &&   //right edge of player
                bullet.getPositionX() + 5 > player.getPositionX() &&    //left edge of player
                bullet.getPositionY() < player.getPositionY() + 20 &&   //bottom edge of player
                bullet.getPositionY() + 10 > player.getPositionY()) {   //top edge of player
            System.out.println("Player hit by alien bullet!");
            //removes bullet from game
            detector.getBullets().remove(bullet);
            //decrease players live using lives manager
            detector.getLivesManager().decreaseLives();
        } else if (nextHandler != null) {
            //pass the collision handling to the next handler in the chain
            nextHandler.handleCollision(bullet, player, aliens, shields, specialAlien, specialAlienActive, detector);
        }
    }

    // sets next handler
    @Override
    public void setNextHandler(CollisionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
