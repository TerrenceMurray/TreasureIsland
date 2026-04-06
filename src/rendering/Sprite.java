package rendering;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Sprite {

    protected BufferedImage image;

    public Sprite(String path) {
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Could not load sprite: " + path);
        }
    }

    public Sprite(BufferedImage image) {
        this.image = image;
    }

    public void draw(Graphics2D g, int x, int y, int width, int height) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    public void drawFlipped(Graphics2D g, int x, int y, int width, int height) {
        if (image != null) {
            g.drawImage(image, x + width, y, -width, height, null);
        }
    }

    public int getWidth() { return image != null ? image.getWidth() : 0; }
    public int getHeight() { return image != null ? image.getHeight() : 0; }
}
