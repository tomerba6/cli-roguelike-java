package dnd.board;

import dnd.units.enemy.Enemy;
import dnd.units.enemy.EnemyFactory;
import dnd.units.player.Player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses level text files into fully initialized Level objects.
 * <p>
 * Uses a two-pass algorithm: the first pass builds the terrain
 * grid (Walls and Floors), and the second pass spawns occupants
 * (the Player and all Enemies) onto the correct cells.
 */
public class LevelLoader {

    /**
     * Parses a level file and returns a ready-to-play Level.
     *
     * @param filePath the absolute path to the .txt level file
     * @param player   the player instance to place at the '@' spawn point
     * @return a {@link Level} containing the board and the active enemy list
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is empty or contains an unknown tile character
     */
    public Level loadLevel(String filePath, Player player) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Level file is empty!");
        }

        int height = lines.size();
        int width = lines.get(0).length();

        Cell[][] grid = new Cell[height][width];
        List<Enemy> activeEnemies = new ArrayList<>();

        // PASS 1: Build the raw structural terrain (Walls and Empty Floors)
        for (int y = 0; y < height; y++) {
            String row = lines.get(y);
            for (int x = 0; x < width; x++) {
                char tileChar = row.charAt(x);
                Position currentPos = new Position(x, y);

                if (tileChar == '#') {
                    grid[y][x] = new Wall();
                } else {
                    // Everything else rests on a floor
                    grid[y][x] = new Floor(currentPos);
                }
            }
        }

        GameBoard board = new GameBoard(grid);

        // PASS 2: Spawn the specific occupants
        for (int y = 0; y < height; y++) {
            String row = lines.get(y);
            for (int x = 0; x < width; x++) {
                char tileChar = row.charAt(x);
                Position currentPos = new Position(x, y);

                if (tileChar == '@') {
                    // Spawn Player
                    player.setPosition(currentPos);
                    board.setOccupant(currentPos, player);
                } else if (tileChar != '#' && tileChar != '.') {
                    // Spawn Enemy
                    Enemy spawnedEnemy = EnemyFactory.createEnemy(tileChar);
                    if (spawnedEnemy != null) {
                        spawnedEnemy.setPosition(currentPos);
                        board.setOccupant(currentPos, spawnedEnemy);
                        activeEnemies.add(spawnedEnemy);
                    } else {
                        throw new IllegalArgumentException("Unknown character in level file: " + tileChar);
                    }
                }
            }
        }

        return new Level(board, activeEnemies);
    }
}