package dnd.board;

import dnd.units.enemy.Enemy;
import java.util.List;

/**
 * A container holding the completely parsed state of a single game level.
 */
public class Level {
    private final GameBoard board;
    private final List<Enemy> activeEnemies;

    /**
     * Constructs a Level from a pre-built board and enemy list.
     *
     * @param board         the fully parsed GameBoard for this level
     * @param activeEnemies the mutable list of enemies still alive on the board
     */
    public Level(GameBoard board, List<Enemy> activeEnemies) {
        this.board = board;
        this.activeEnemies = activeEnemies;
    }

    /** @return the GameBoard containing the terrain and occupant grid */
    public GameBoard getBoard() { return board; }

    /** @return the live list of enemies; modified in place as enemies are killed */
    public List<Enemy> getActiveEnemies() { return activeEnemies; }
}