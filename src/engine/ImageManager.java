package engine;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ImageManager {

    private static ImageManager instance;
    private Map<String, BufferedImage> images;

    private ImageManager() {
        images = new HashMap<>();
    }

    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    public void loadImage(String key, String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Image not found: " + path);
                return;
            }
            images.put(key, ImageIO.read(is));
        } catch (IOException e) {
            System.err.println("Failed to load image: " + path);
            e.printStackTrace();
        }
    }

    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}
