package engine;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Centralizes image loading and processing (Week 3 pattern).
public class ImageManager {

    public ImageManager() {
    }

    public static Image loadImage(String fileName) {
        return new ImageIcon(fileName).getImage();
    }

    public static BufferedImage loadBufferedImage(String fileName) {
        BufferedImage bi = null;
        File file = new File(fileName);
        try {
            bi = ImageIO.read(file);
        } catch (IOException ioe) {
            System.err.println("Error opening file " + fileName + ": " + ioe);
        }
        return bi;
    }

    // Make a copy of the BufferedImage src.
    public static BufferedImage copyImage(BufferedImage src) {
        if (src == null) return null;

        BufferedImage copy = new BufferedImage(
            src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(src, 0, 0, null);
        g2d.dispose();

        return copy;
    }
}
