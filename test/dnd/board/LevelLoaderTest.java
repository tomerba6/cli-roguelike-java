package dnd.board;

import dnd.units.DummyPlayer;
import dnd.units.enemy.Enemy;
import dnd.units.enemy.Monster;
import dnd.units.enemy.Trap;
import dnd.units.enemy.Boss;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LevelLoaderTest {

    private LevelLoader levelLoader;
    private DummyPlayer player;

    @BeforeEach
    public void setUp() {
        levelLoader = new LevelLoader();
        player = new DummyPlayer("Hero", 100, 10, 10);
    }

    // --- PARSING TESTS ---

    /** Parses a minimal map: verifies correct board dimensions and wall/floor cell types at known positions. */
    @Test
    public void testBasicBoardParsing(@TempDir Path tempDir) throws IOException {
        // 1. Create a temporary text file simulating a basic map
        Path levelPath = tempDir.resolve("level1.txt");
        Files.write(levelPath, Arrays.asList(
                "###",
                "#@.",
                "###"
        ));

        // 2. Load the level
        Level level = levelLoader.loadLevel(levelPath.toString(), player);
        GameBoard board = level.getBoard();

        // 3. Verify Wall mapping
        assertTrue(board.getCell(new Position(0, 0)) instanceof Wall, "(0,0) should be a Wall");
        assertTrue(board.getCell(new Position(2, 2)) instanceof Wall, "(2,2) should be a Wall");

        // 4. Verify Player (@) spawn mapping
        Position spawnPos = new Position(1, 1);
        assertTrue(board.getCell(spawnPos) instanceof Floor, "Spawn point must be built on a Floor");
        assertEquals(player, board.getOccupant(spawnPos), "Player should be the occupant of the spawn floor");
        assertEquals(spawnPos, player.getPosition(), "Player's internal coordinates must be updated to the spawn point");

        // 5. Verify empty Floor (.) mapping
        assertNull(board.getOccupant(new Position(2, 1)), "Empty floor should have no occupant");
        assertTrue(level.getActiveEnemies().isEmpty(), "There should be no enemies in this list");
    }

    /** Enemy characters spawn the correct Enemy subclass via the factory and appear in the active list. */
    @Test
    public void testEnemySpawningAndFactoryIntegration(@TempDir Path tempDir) throws IOException {
        // 1. Create a map with one Monster ('s'), one Trap ('D'), and one Boss ('M')
        Path levelPath = tempDir.resolve("level2.txt");
        Files.write(levelPath, Arrays.asList(
                "s.D",
                "..M"
        ));

        Level level = levelLoader.loadLevel(levelPath.toString(), player);
        GameBoard board = level.getBoard();
        List<Enemy> enemies = level.getActiveEnemies();

        // 2. Verify the enemy list populated correctly
        assertEquals(3, enemies.size(), "Level should contain exactly 3 enemies");

        // 3. Verify specific instances via grid coordinates
        assertTrue(board.getOccupant(new Position(0, 0)) instanceof Monster, "'s' should spawn a Monster");
        assertTrue(board.getOccupant(new Position(2, 0)) instanceof Trap, "'D' should spawn a Trap");
        assertTrue(board.getOccupant(new Position(2, 1)) instanceof Boss, "'M' should spawn a Boss");

        // Ensure their internal positions map correctly
        assertEquals(new Position(0, 0), board.getOccupant(new Position(0,0)).getPosition());
    }

    // --- EXCEPTION HANDLING TESTS ---

    /** Empty file throws the documented exception rather than returning null or an empty board. */
    @Test
    public void testEmptyFileThrowsException(@TempDir Path tempDir) throws IOException {
        Path levelPath = tempDir.resolve("empty.txt");
        Files.createFile(levelPath); // Creates a totally blank file

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            levelLoader.loadLevel(levelPath.toString(), player);
        });
        assertTrue(exception.getMessage().contains("empty"), "Should catch empty files instantly");
    }

    /** Unknown tile character throws the documented exception. */
    @Test
    public void testUnknownCharacterThrowsException(@TempDir Path tempDir) throws IOException {
        Path levelPath = tempDir.resolve("corrupted.txt");
        // 'X' is not a valid map tile according to our rules (X is dead player, not map tile)
        Files.write(levelPath, Arrays.asList(
                "###",
                "#X#",
                "###"
        ));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            levelLoader.loadLevel(levelPath.toString(), player);
        });
        assertTrue(exception.getMessage().contains("Unknown character"), "Should crash on invalid ASCII characters");
    }

    /** Level file with no '@' marker loads without throwing; player position is left at its default. */
    @Test
    public void testMissingPlayerSpawnDoesNotThrow(@TempDir Path tempDir) throws IOException {
        // A level file with no '@' is technically valid structure-wise — the loader
        // simply never calls player.setPosition(), leaving it as null.
        Path levelPath = tempDir.resolve("noSpawn.txt");
        Files.write(levelPath, Arrays.asList(
                "###",
                "#.#",
                "###"
        ));

        assertDoesNotThrow(() -> {
            Level level = levelLoader.loadLevel(levelPath.toString(), player);
            assertEquals(0, level.getActiveEnemies().size(), "No enemies should be spawned in a no-enemy map");
            assertNull(player.getPosition(), "Player position should remain null when '@' is absent from the level file");
        });
    }
}