package dnd.board;

import dnd.units.enemy.Enemy;
import java.util.List;

/**
 * A container holding the completely parsed state of a single game level.
 */
public class Level {
    private final GameBoard board;
    private final List<Enemy> activeEnemies;

    public Level(GameBoard board, List<Enemy> activeEnemies) {
        this.board = board;
        this.activeEnemies = activeEnemies;
    }

    public GameBoard getBoard() { return board; }
    public List<Enemy> getActiveEnemies() { return activeEnemies; }
}