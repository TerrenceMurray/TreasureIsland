package engine;

/**
    The GameStateManager class is a singleton that tracks the
    current game state (MENU, PLAYING, PAUSED, LEVEL_COMPLETE,
    GAME_OVER, VICTORY) and the player's score across levels.
*/
public class GameStateManager {

    private static GameStateManager instance;

    private String currentState;
    private int score;

    private GameStateManager() {
        currentState = "MENU";
        score = 0;
    }

    /**
        Returns the shared GameStateManager instance, creating it
        on first use.
    */
    public static GameStateManager getInstance() {
        if (instance == null) {
            instance = new GameStateManager();
        }
        return instance;
    }

    public void setState(String state) {
        this.currentState = state;
    }

    public String getState() {
        return currentState;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public int getScore() {
        return score;
    }

    public void resetScore() {
        this.score = 0;
    }
}
