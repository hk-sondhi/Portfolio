import javax.swing.*;
import java.awt.*;


public class GameOverScreen extends JPanel {
    private final JFrame frame; // Marked as 'final' since it doesn't change
    private final GameEngine gameEngine; // Marked as 'final' since it doesn't change

    public GameOverScreen(JFrame frame, GameEngine gameEngine, int score) {
        this.frame = frame;
        this.gameEngine = gameEngine;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // Game Over Label
        JLabel gameOverLabel = new JLabel("Game Over", SwingConstants.CENTER);
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 48));
        add(gameOverLabel, BorderLayout.NORTH);

        // Score Label
        JLabel scoreLabel = new JLabel("Your Score: " + score, SwingConstants.CENTER);
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 24));
        add(scoreLabel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        JButton restartButton = new JButton("Restart");
        JButton exitButton = new JButton("Exit");

        restartButton.addActionListener(e -> restartGame());
        exitButton.addActionListener(e -> System.exit(0)); // Exit the program

        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void restartGame() {
        frame.getContentPane().removeAll(); // Clear the frame
        GameEngine newGameEngine = new GameEngine(); // Start a new game
        frame.add(newGameEngine.getGameBoard()); // Add the new game board
        newGameEngine.startGame();
        frame.revalidate();
        gameEngine.reset(); // Reset the current game engine
    }
}
