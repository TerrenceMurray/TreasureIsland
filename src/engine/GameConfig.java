package engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GameConfig {

    private static GameConfig instance;
    private Properties props;

    private GameConfig() {
        props = new Properties();
        try (FileInputStream fis = new FileInputStream("config/game.properties")) {
            props.load(fis);
        } catch (IOException e) {
            System.err.println("Could not load game.properties, using defaults");
        }
    }

    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    public int getInt(String key, int defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        return Integer.parseInt(val.trim());
    }

    public float getFloat(String key, float defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        return Float.parseFloat(val.trim());
    }

    public String getString(String key, String defaultVal) {
        return props.getProperty(key, defaultVal);
    }
}
