import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import engine.GamePanel;

/**
    The Main class is the entry point for Treasure Island.
    It creates the application window, adds the GamePanel, and
    starts the game loop on the Swing event dispatch thread.
*/
public class Main {

    /**
        Launches the game window and starts the game loop.
    */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Treasure Island");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            GamePanel gameLoop = new GamePanel();
            window.add(gameLoop);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            gameLoop.startGame();
        });
    }
}
