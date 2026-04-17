package engine;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
    The ImageManager class centralizes image loading and simple
    image processing. Following the Week 3 pattern, all methods
    are static utilities so the rest of the code doesn't need to
    manage ImageIO or ImageIcon directly.
*/
public class ImageManager {

    public ImageManager() {
    }

    /**
        Loads an Image from the given file path using ImageIcon.
    */
    public static Image loadImage(String fileName) {
        return new ImageIcon(fileName).getImage();
    }

    /**
        Loads a BufferedImage from the given file path. Returns
        null if the file cannot be read.
    */
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

    /**
        Returns an independent copy of the given BufferedImage,
        or null if src is null.
    */
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
