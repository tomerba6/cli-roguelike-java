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

public class LevelLoader {

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