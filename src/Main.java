import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import engine.GameLoop;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Treasure Island");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);

            GameLoop gameLoop = new GameLoop();
            window.add(gameLoop);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);

            gameLoop.start();
        });
    }
}
