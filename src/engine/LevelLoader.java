package engine;

import entities.collectibles.Collectible;
import entities.collectibles.Diamond;
import entities.collectibles.HealthPotion;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class LevelLoader {

    private int levelWidth = 960;
    private List<Rectangle> platforms = new ArrayList<>();
    private List<Collectible> collectibles = new ArrayList<>();
    private List<float[]> pinkStarSpawns = new ArrayList<>();
    private List<float[]> crabbySpawns = new ArrayList<>();

    public LevelLoader(String filePath) {
        load(filePath);
    }

    private void load(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String section = "";
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("[")) {
                    section = line.substring(1, line.indexOf(']'));
                    continue;
                }

                String[] parts = line.split(",");
                switch (section) {
                    case "level":
                        if (parts[0].trim().equals("width")) {
                            levelWidth = Integer.parseInt(parts[1].trim());
                        }
                        break;
                    case "platforms":
                        platforms.add(new Rectangle(
                            Integer.parseInt(parts[0].trim()),
                            Integer.parseInt(parts[1].trim()),
                            Integer.parseInt(parts[2].trim()),
                            Integer.parseInt(parts[3].trim())
                        ));
                        break;
                    case "diamonds":
                        collectibles.add(new Diamond(
                            Float.parseFloat(parts[0].trim()),
                            Float.parseFloat(parts[1].trim())
                        ));
                        break;
                    case "potions":
                        collectibles.add(new HealthPotion(
                            Float.parseFloat(parts[0].trim()),
                            Float.parseFloat(parts[1].trim())
                        ));
                        break;
                    case "pinkstars":
                        pinkStarSpawns.add(new float[]{
                            Float.parseFloat(parts[0].trim()),
                            Float.parseFloat(parts[1].trim())
                        });
                        break;
                    case "crabbies":
                        crabbySpawns.add(new float[]{
                            Float.parseFloat(parts[0].trim()),
                            Float.parseFloat(parts[1].trim())
                        });
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load level file: " + filePath);
            e.printStackTrace();
        }
    }

    public int getLevelWidth() { return levelWidth; }
    public List<Rectangle> getPlatforms() { return platforms; }
    public List<Collectible> getCollectibles() { return collectibles; }
    public List<float[]> getPinkStarSpawns() { return pinkStarSpawns; }
    public List<float[]> getCrabbySpawns() { return crabbySpawns; }
}
