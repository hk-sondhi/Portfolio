
import java.util.ArrayList;

//handles collisons between bullets and aliens by implementing the collion handler interface
public class AlienCollisionHandler implements CollisionHandler {
    private CollisionHandler nextHandler;   //if current handler cant process collision, delegates to next handler

    @Override
    public void handleCollision(Bullet bullet, Player player, ArrayList<Alien> aliens, ArrayList<Shield> shields, Alien specialAlien, boolean specialAlienActive, CollisionDetector detector) {
        //checks if bullet collides with alien
        for (Alien alien : aliens) {
            if (bullet.getOwner().equals("player") &&
                    bullet.getPositionX() < alien.getPositionX() + 40 &&
                    bullet.getPositionX() + 5 > alien.getPositionX() &&
                    bullet.getPositionY() < alien.getPositionY() + 20 &&
                    bullet.getPositionY() + 10 > alien.getPositionY()) {
                aliens.remove(alien);   //removes alien from game
                detector.getBullets().remove(bullet);   //removes bullet that collided
                detector.getScoreManager().addScore(alien.getPositionY() <= 110 ? 30 : 10); //updates player score
                return;
            }
        }

        //if current handler cannot process collison, delegates to nexthandler
        if (nextHandler != null) {
            nextHandler.handleCollision(bullet, player, aliens, shields, specialAlien, specialAlienActive, detector);
        }
    }

    //sets next handler
    @Override
    public void setNextHandler(CollisionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
}
