import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

// The InputHandler class implements the KeyListener interface to handle keyboard inputs.
// By interpreting key presses and delegating actions to the game objects (player, bullets).
public class InputHandler implements KeyListener {
    private Player player;  //player actions
    private ArrayList<Bullet> bullets;  //bullet actions

    //constructor to initialise the input handler
    public InputHandler(Player player, ArrayList<Bullet> bullets) {
        this.player = player;
        this.bullets = bullets;
    }

    // Handles key press events
    @Override
    public void keyPressed(KeyEvent e) {
        // Uses the factory method to get and perform the key action
        GameObjectFactory.KeyAction action = GameObjectFactory.getKeyAction(e.getKeyCode(), player, bullets);
        action.performAction(); //performs the action (move player or shoot bullet)
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Handle key release if needed
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed for this implementation
    }
}
