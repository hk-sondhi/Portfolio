import java.util.ArrayList;

//interface to handle collisions in game
public interface CollisionHandler {
    //processes collisions with game objects and updates states
    void handleCollision(Bullet bullet, Player player, ArrayList<Alien> aliens, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive, CollisionDetector detector);
    //Sets the next handler
    void setNextHandler(CollisionHandler nextHandler);
}
