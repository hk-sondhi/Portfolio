import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

//games main visual component for all game objects
public class GameBoard extends JPanel {

    private Player player;  //player object on board
    private ArrayList<Alien> aliens;    //list of all aliens currently active in game
    private ArrayList<Bullet> bullets;  //list of bullets currently active in game
    private ArrayList<Shield> shields;  //list of shields currently in game
    private Alien specialAlien;
    private boolean specialAlienActive;
    private int lives;  //tracks remaining players lives
    private int score;  //players current score
    private Image lifeIcon; //icon used to represent remaining lives on the board

    //initialises game board using factory class
    public GameBoard() {
        setPreferredSize(new Dimension(800, 600));
        aliens = new ArrayList<>();
        bullets = new ArrayList<>();
        shields = new ArrayList<>();
        player = null;
        specialAlien = null;
        specialAlienActive = false;
        lives = 0;
        score = 0;

        lifeIcon = GameObjectFactory.createLifeIcon("/Images/life_icon.png");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        //Draws the player image at the player's current position
        if (player != null) {
            g.drawImage(player.getPlayerImage(), player.getPositionX(), player.getPositionY(), 50, 50, null);
        }

        //Iterates through the aliens list and renders each alien at its respective position
        if (aliens != null) {
            for (Alien alien : aliens) {
                g.drawImage(alien.getAlienImage(), alien.getPositionX(), alien.getPositionY(), 40, 40, null);
            }
        }

        //Draws each bullet as an orange rectangle
        if (bullets != null) {
            g.setColor(Color.ORANGE);
            for (Bullet bullet : bullets) {
                g.fillRect(bullet.getPositionX(), bullet.getPositionY(), 5, 10);
            }
        }

        if (shields != null) {
            for (Shield shield : shields) {
                shield.draw(g);
            }
        }

        if (specialAlienActive && specialAlien != null) {
            g.drawImage(specialAlien.getAlienImage(), specialAlien.getPositionX(), specialAlien.getPositionY(), 40, 40, null);
        }

        //displays score at top left corner of screen using factory class
        g.setColor(Color.WHITE);
        g.drawString(GameObjectFactory.formatScore(score), 10, 20);

        //Draws the life icons in the bottom-right corner, with one icon per remaining life.
        if (lifeIcon != null) {
            for (int i = 0; i < lives; i++) {
                g.drawImage(lifeIcon, 650 + i * 40, 550, 30, 30, null);
            }
        }
    }

    //updates the game state with new data and triggers a repaint to render the updated state
    public void render(Player player, ArrayList<Alien> aliens, ArrayList<Bullet> bullets, int score, int lives, Alien specialAlien, boolean specialAlienActive, ArrayList<Shield> shields) {
        this.player = player;
        this.aliens = aliens != null ? aliens : new ArrayList<>();
        this.bullets = bullets != null ? bullets : new ArrayList<>();
        this.score = score;
        this.lives = lives;
        this.specialAlien = specialAlien;
        this.specialAlienActive = specialAlienActive;
        this.shields = shields != null ? shields : new ArrayList<>();
        repaint();
    }
}
