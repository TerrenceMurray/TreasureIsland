package engine.managers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
    The GameConfig class is a singleton that loads tunable game
    values from config/game.properties. Gameplay code reads values
    through getInt/getFloat so numbers can be tweaked without
    recompiling.
*/
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

    /**
        Returns the shared GameConfig instance, creating it on first
        use.
    */
    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    /**
        Reads an int property by key, returning defaultVal if the
        key is missing.
    */
    public int getInt(String key, int defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        return Integer.parseInt(val.trim());
    }

    /**
        Reads a float property by key, returning defaultVal if the
        key is missing.
    */
    public float getFloat(String key, float defaultVal) {
        String val = props.getProperty(key);
        if (val == null) return defaultVal;
        return Float.parseFloat(val.trim());
    }

}
