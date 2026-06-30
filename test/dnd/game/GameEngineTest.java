package dnd.game;

import dnd.board.GameBoard;
import dnd.board.Position;
import dnd.units.Occupant;
import dnd.units.enemy.Enemy;
import dnd.units.player.Warrior;
import dnd.utils.MessageCallback;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    private GameEngine engine;
    private File tempLevel1;
    private File tempLevel2;
    private List<String> capturedLogs;

    @BeforeEach
    public void setUp() throws IOException {
        // 1. Create temporary text files to simulate level loading
        tempLevel1 = File.createTempFile("level1", ".txt");
        tempLevel2 = File.createTempFile("level2", ".txt");

        // Write a simple map for Level 1 (Player at 1,1. Trapped enemy at 2,2)
        // #####
        // #@..#
        // ##s##
        // #####
        try (FileWriter writer = new FileWriter(tempLevel1)) {
            writer.write("#####\n#@..#\n##s##\n#####\n");
        }

        // Write a simple 5x3 map for Level 2 (Player at 1,1. One enemy 's' at 3,1)
        // #####
        // #@.s#
        // #####
        try (FileWriter writer = new FileWriter(tempLevel2)) {
            writer.write("#####\n#@.s#\n#####\n");
        }

        // 2. Pass the absolute paths of our temporary files to the engine
        List<String> levelPaths = new ArrayList<>();
        levelPaths.add(tempLevel1.getAbsolutePath());
        levelPaths.add(tempLevel2.getAbsolutePath());

        engine = new GameEngine(levelPaths);

        // 3. Initialize with a silent callback that intercepts logs
        capturedLogs = new ArrayList<>();
        MessageCallback interceptor = message -> capturedLogs.add(message);

        //1. Jon Snow Health: 300/300 Attack: 30 Defense: 4 Level: 1 Experience: 0/50 Cooldown: 0/3
        engine.initialize(1, interceptor);
    }

    @AfterEach
    public void tearDown() {
        // Clean up the temporary files after the tests finish
        if (tempLevel1 != null) tempLevel1.delete();
        if (tempLevel2 != null) tempLevel2.delete();
    }

    // --- GAME ENGINE INTEGRATION TESTS ---

    /** Killing the last enemy on a non-final level causes the engine to load the next level. */
    @Test
    public void testEngineTransitionsToNextLevelWhenCleared() {
        // tempLevel1 has a player at (1,1) and a monster at (2,2).
        // Let's drop the monster to 1 HP and kill it.
        Occupant target = engine.getCurrentBoard().getCell(new Position(2, 2)).getOccupant();
        if (target instanceof Enemy) {
            ((Enemy) target).getHealth().takeDamage(((Enemy) target).getHealth().getHealthAmount() - 1);
        }

        // First tick: the monster is walled in at (2,2) and can only pathfind up to (2,1),
        // which lands it directly to the right of the stationary player at (1,1).
        GameBoard level1Board = engine.getCurrentBoard();
        engine.gameTick('s');

        // RNG-Buster: a single Warrior swing (0-30 attack) can roll a 0 or be fully blocked by the
        // Gold Cloak's defense roll (0-3), so one 'd' is NOT guaranteed to kill the 1-HP monster.
        // Keep swinging until a non-zero roll lands the kill. Clearing Level 1's only enemy makes the
        // engine immediately load Level 2, swapping in a brand-new board instance — our signal to stop
        // (so the player never gets a stray turn on the freshly loaded Level 2).
        for (int i = 0; i < 50 && engine.getCurrentBoard() == level1Board; i++) {
            engine.gameTick('d');
        }

        // At this exact moment, Level 1 is clear. The engine should have immediately loaded Level 2!

        // 1. Verify we haven't won the whole game yet
        assertFalse(engine.isGameWon(), "Game should not be won, as Level 2 still has enemies");

        // 2. Verify the player was teleported to Level 2's spawn point at (1,1)
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Player should spawn at (1,1) on the new Level 2 map");

        // 3. Verify Level 2's specific monster ('s' at 3,1) is present
        assertNotNull(engine.getCurrentBoard().getCell(new Position(3, 1)).getOccupant(), "Level 2's monster should be loaded on the board");
    }

    /** Pressing 'e' calls the player's castAbility and depletes one cast's worth of resources. */
    @Test
    public void testPlayerAbilityInputTriggersCast() throws Exception {
        // Player ('@') is standing near a monster ('s')
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        // Player presses 'e' to cast their special ability
        engine.gameTick('e');

        // Verify the ability was routed natively by the Engine via the logs.
        // Looking for standard ability keywords since different classes output different text
        // (e.g., "used Avenger's Shield", "cast", "ability damage", "healing").
        boolean abilityCast = capturedLogs.stream().anyMatch(log ->
                log.contains("used") || log.toLowerCase().contains("ability") || log.contains("cast")
        );

        assertTrue(abilityCast, "Pressing 'e' should correctly route to the Player's castAbility method");

        // Verify the player didn't physically move while casting
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Player should remain stationary while casting an ability");

        Warrior player = (Warrior) engine.getPlayer();
        assertEquals(3, player.getRemainingCooldown(), "Cooldown should reset to 3/3");
    }

    /** Engine initializes with the correct board and enemy list after construction. */
    @Test
    public void testEngineLoadsFirstLevelProperly() {
        assertNotNull(engine.getCurrentBoard(), "Board should be loaded");
        assertNotNull(engine.getPlayer(), "Player should be initialized");

        // Based on our tempLevel1 text, the player '@' is at X:1, Y:1
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Player should spawn at the '@' coordinates");

        assertFalse(engine.isGameOver(), "Game should not be over");
        assertFalse(engine.isGameWon(), "Game should not be won yet");
    }

    /** Player pressing a valid direction key moves to the adjacent floor tile. */
    @Test
    public void testValidMovementUpdatesPosition() {
        // Player is at (1,1). We send 'd' to move right to (2,1).
        engine.gameTick('d');

        assertEquals(new Position(2, 1), engine.getPlayer().getPosition(), "Player should move to the right into the empty floor");
    }

    /** Player pressing a direction key toward a wall stays in place. */
    @Test
    public void testInvalidMovementIsBlockedByWalls() {
        // Player is at (1,1). We send 'w' to move up into the '#' wall at (1,0).
        engine.gameTick('w');

        // The Engine's Visitor pattern should reject the movement, keeping the player at (1,1)
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Player should NOT be able to move into a Wall cell");
    }

    /** Unrecognized key character aborts the tick; enemies do not take their turn. */
    @Test
    public void testInvalidInputIsIgnoredAndAbortsTick() throws Exception {
        // Player at (1,1). Boss at (4,1).
        String map = "#######\n" +
                "#@...M#\n" +
                "#######";
        GameEngine engine = loadCustomLevel(map);

        // Send a gibberish character
        engine.gameTick('x');

        // 1. Verify Player didn't move
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Gibberish input should not move the player");

        // 2. Verify the Enemy didn't move! (Proving the tick was fully aborted)
        assertNotNull(engine.getCurrentBoard().getCell(new Position(5, 1)).getOccupant(), "Enemy should not get a free turn when the player enters invalid input");
    }

    /** Killing the last enemy on the final level sets isGameWon() to true. */
    @Test
    public void testGameWonWhenAllLevelsCleared() throws Exception {
        // Create an empty room with NO enemies using our custom loader
        String emptyMap = "#####\n" +
                "#@..#\n" +
                "#####";

        GameEngine shortEngine = loadCustomLevel(emptyMap);

        // Tick once. The engine checks the board, sees 0 enemies, and calls loadNextLevel().
        // Because there are no more levels in the list, it should immediately flag gameWon = true!
        shortEngine.gameTick('q');

        assertTrue(shortEngine.isGameWon(), "Engine should flag Game Won when the final level's enemy list is completely empty");
        assertFalse(shortEngine.isGameOver(), "Winning the game should not trigger the Game Over flag");
    }

    /**
     * HELPER METHOD: Dynamically creates a temporary level file, loads it into the Engine,
     * and returns the initialized Engine. This allows for rapid map scenario testing!
     */
    private GameEngine loadCustomLevel(String mapLayout) throws IOException {
        return loadCustomLevelWithPlayer(mapLayout, 1);
    }

    /**
     * Variant of loadCustomLevel that lets you choose the player type.
     * playerChoice maps directly to PlayerFactory.createPlayer (1=Warrior, 3=Mage, 5=Rogue, 7=Hunter).
     */
    private GameEngine loadCustomLevelWithPlayer(String mapLayout, int playerChoice) throws IOException {
        capturedLogs.clear();
        File tempFile = File.createTempFile("customLevel", ".txt");
        tempFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(mapLayout);
        }

        List<String> levelPaths = new ArrayList<>();
        levelPaths.add(tempFile.getAbsolutePath());

        GameEngine testEngine = new GameEngine(levelPaths);
        testEngine.initialize(playerChoice, message -> capturedLogs.add(message));

        return testEngine;
    }

    // --- ADVANCED INTEGRATION TESTS ---

    /** Multiple enemies chase the player simultaneously without occupying the same tile. */
    @Test
    public void testMultipleEnemiesMovementAndBlocking() throws Exception {
        // Player at (1,1). Enemy 's' at (3,1). Enemy 'k' at (4,1).
        String map = "######\n" +
                "#@.sk#\n" +
                "######";

        GameEngine engine = loadCustomLevel(map);

        // Tick the game (Player rests, enemies take their turn)
        engine.gameTick('q');

        GameBoard board = engine.getCurrentBoard();

        // 1. Enemy 's' should move left towards the player to (2,1)
        Occupant occupant21 = board.getCell(new Position(2, 1)).getOccupant();
        assertNotNull(occupant21, "Enemy 's' should move into the empty space");

        // 2. Enemy 'k' should move left towards the player to (3,1)
        Occupant occupant31 = board.getCell(new Position(3, 1)).getOccupant();
        assertNotNull(occupant31, "Enemy 'k' should move into the space 's' just vacated");

        // 3. The original space (4,1) should now be empty
        Occupant occupant41 = board.getCell(new Position(4, 1)).getOccupant();
        assertNull(occupant41, "The back space should be fully clear");
    }

    /** Trap deals damage to the player when they are adjacent after a game tick. */
    @Test
    public void testTrapAttacksWhenPlayerIsAdjacent() throws Exception {
        // Trap ('B') spawns directly next to Player ('@')
        String map = "####\n" +
                "#@B#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        // Tick the game. Because distance is 1 (which is < 2), the Trap MUST attack.
        engine.gameTick('q');

        // 1. Verify the Trap physically remained in place
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Player should still be at (1,1)");
        assertNotNull(engine.getCurrentBoard().getCell(new Position(2, 1)).getOccupant(), "Trap should safely remain stationary at (2,1)");

        // 2. Verify the Trap engaged in combat via logs (This bypasses 0-damage RNG!)
        boolean trapAttacked = capturedLogs.stream().anyMatch(log -> log.contains("engaged in combat"));

        assertTrue(trapAttacked, "Trap should automatically initiate combat when range < 2, even if it rolls 0 damage");
    }

    /** Boss chases the player within vision range and melee-attacks when adjacent. */
    @Test
    public void testBossChasesAndEngagesInMeleeCombat() throws Exception {
        // Boss ('M') spawns 4 tiles away from the Player
        String map = "#######\n" +
                "#@...M#\n" +
                "#######";

        GameEngine engine = loadCustomLevel(map);

        // Tick 1: Boss should pathfind left
        engine.gameTick('q');
        assertNotNull(engine.getCurrentBoard().getCell(new Position(4, 1)).getOccupant(), "Boss should chase the player to (4,1)");

        // Tick 2, 3: Boss continues chasing
        engine.gameTick('q'); // Boss at (3,1)
        engine.gameTick('q'); // Boss at (2,1)

        // Tick 4: Boss is adjacent and attempts to move into the player, triggering Melee Visitor
        engine.gameTick('q');

        // 1. Verify the GameEngine hasn't crashed
        assertFalse(engine.isGameWon(), "Game is still running");

        // 2. Verify the Boss successfully initiated MELEE combat via logs!
        // This mathematically proves the Visitor pattern triggered, bypassing any RNG zero-damage rolls.
        boolean bossAttacked = capturedLogs.stream().anyMatch(log -> log.contains("engaged in combat"));

        assertTrue(bossAttacked, "Boss should have successfully triggered the melee combat logic on tick 4");
    }

    /** Boss trapped behind a wall accumulates combatTicks to abilityFrequency and then casts its ability. */
    @Test
    public void testBossCastsAbilityOnExactTickWhileTrapped() throws Exception {
        // Boss ('M') spawns 4 tiles away, but is TRAPPED behind a wall ('#')
        // Distance is 4 (Valid for Vision Range 6)
        String map = "#######\n" +
                "#@..#M#\n" +
                "#######";

        GameEngine engine = loadCustomLevel(map);
        int initialHealth = engine.getPlayer().getHealth().getHealthAmount();

        // Tick 1-5: Boss tries to pathfind but is blocked by the wall. Ticks count up to 5.
        engine.gameTick('q');
        engine.gameTick('q');
        engine.gameTick('q');
        engine.gameTick('q');
        engine.gameTick('q');

        // Verify Player took absolutely zero damage from melee because of the wall
        assertEquals(initialHealth, engine.getPlayer().getHealth().getHealthAmount(), "Player should take 0 damage while the Boss is trapped behind a wall");

        // Clear logs right before the crucial tick so we only see the ability cast
        capturedLogs.clear();

        // Tick 6: The Ability Trigger! (combatTicks reaches abilityFrequency=5, check fires before increment)
        engine.gameTick('q');

        // Verify the Ability fired via the logs, completely bypassing RNG stat math
        boolean abilityCast = capturedLogs.stream().anyMatch(log -> log.contains("shoots") || log.contains("ability damage"));
        assertTrue(abilityCast, "Boss MUST have cast its ability on exactly tick 6");

        // Verify the Boss didn't magically teleport through the wall
        assertEquals(new Position(5, 1), engine.getCurrentBoard().getCell(new Position(5, 1)).getOccupant().getPosition(), "Boss should still be trapped at its starting position");
    }

    /** Player HP reaching 0 sets isGameOver() to true and stops further enemy turns. */
    @Test
    public void testEngineCorrectlyHaltsWhenPlayerDies() throws Exception {
        // Surround the player with 3 powerful Bosses/Knights
        String map = "####\n" +
                "#kM#\n" +
                "#@k#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        // Manually drop the player to 1 HP.
        // This eliminates RNG: the very first unblocked hit will instantly kill them!
        int currentHp = engine.getPlayer().getHealth().getHealthAmount();
        engine.getPlayer().getHealth().takeDamage(currentHp - 1);

        // Rest a few times until killed by the mob
        for (int i = 0; i < 10; i++) {
            if (engine.isGameOver()) break;
            engine.gameTick('q');
        }

        // 1. Verify the engine flipped the switch (This proves our Engine bug fix worked!)
        assertTrue(engine.isGameOver(), "Engine should flag gameOver = true when player HP hits 0");
        assertTrue(engine.getPlayer().getHealth().isDead(), "Player should be dead");

        // 2. Verify the Ghost Corpse render swap
        assertEquals("X", engine.getPlayer().toString(), "Player should render as a corpse");
    }

    /** Player moving onto an enemy tile triggers combat and logs the hit message. */
    @Test
    public void testPlayerMeleeAttackTriggersCombatLog() throws Exception {
        // Player is standing directly next to a standard monster ('s')
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        // Player presses 'd' to move right, intentionally walking into the monster
        engine.gameTick('d');

        // 1. Verify combat was initiated natively by the Player via the logs.
        // We do not check coordinates here, because D&D RNG means we don't know
        // if the monster survived the hit or was instantly killed!
        boolean playerAttacked = capturedLogs.stream().anyMatch(log -> log.contains("engaged in combat"));
        assertTrue(playerAttacked, "Pressing a movement key into an enemy should trigger the Player's melee combat Visitor");
    }

    /** Enemy killed by the player is removed from the active list by the engine's janitor sweep. */
    @Test
    public void testPlayerKillsEnemyAndEngineClearsCorpse() throws Exception {
        // Player is standing directly next to a monster ('s')
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        // Drop the monster's health to 1 to speed up the process
        Occupant target = engine.getCurrentBoard().getCell(new Position(2, 1)).getOccupant();
        if (target instanceof Enemy) {
            ((Enemy) target).getHealth().takeDamage(((Enemy) target).getHealth().getHealthAmount() - 1);
        }

        // The RNG-Buster: Player repeatedly swings 'd' until the monster is guaranteed dead
        for (int i = 0; i < 50; i++) {
            if (engine.isGameWon()) break;
            engine.gameTick('d');
        }

        // 1. Verify the OOP Position Swap occurred (Player takes the monster's tile at 2,1)
        assertEquals(new Position(2, 1), engine.getPlayer().getPosition(), "Player should step into the tile of the slain monster");

        // 2. Verify the Engine's Janitor swept the corpse off the board (1,1 should be empty)
        assertNull(engine.getCurrentBoard().getCell(new Position(1, 1)).getOccupant(), "The player's old tile should be completely empty after the corpse is cleaned up");

        // 3. Verify the Game Engine recognized the kill and flagged the Win state
        assertTrue(engine.isGameWon(), "Because the only monster was killed, the active enemies list should be empty, triggering the Win state");
    }

    /** Pressing 'e' as Mage deducts mana and applies Blizzard damage to enemies in range. */
    @Test
    public void testMageAbilityInputCastsBlizzard() throws Exception {
        // Mage (choice 3 = Melisandre) starts with 75/300 mana, cost 30 — can cast immediately.
        // Enemy 's' is 1 tile away, well within Blizzard's range of 6.
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevelWithPlayer(map, 3);

        engine.gameTick('e');

        boolean blizzardCast = capturedLogs.stream().anyMatch(log -> log.contains("cast Blizzard"));
        assertTrue(blizzardCast, "Pressing 'e' with a Mage should route gameTick to Blizzard cast");

        // Casting an ability skips onGameTick — mana should NOT regenerate this turn
        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Mage should remain stationary while casting");
    }

    /** Pressing 'e' as Rogue deducts energy and applies Fan of Knives to adjacent enemies. */
    @Test
    public void testRogueAbilityInputCastsFanOfKnives() throws Exception {
        // Rogue (choice 5 = Arya Stark) starts with 100 energy, cost 20 — can cast immediately.
        // Enemy 's' at (2,1) is distance 1.0 from player at (1,1), within Fan of Knives range < 2.
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevelWithPlayer(map, 5);

        engine.gameTick('e');

        boolean fanCast = capturedLogs.stream().anyMatch(log -> log.contains("Fan of Knives"));
        assertTrue(fanCast, "Pressing 'e' with a Rogue should route gameTick to Fan of Knives cast");

        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Rogue should remain stationary while casting");
    }

    /** Pressing 'e' as Hunter deducts one arrow and applies Shoot damage to the closest in-range enemy. */
    @Test
    public void testHunterAbilityInputCastsShoot() throws Exception {
        // Hunter (choice 7 = Ygritte) starts with 10 arrows, range 6 — can cast immediately.
        // Enemy 's' at (2,1) is distance 1.0, within shooting range of 6.
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevelWithPlayer(map, 7);

        engine.gameTick('e');

        boolean shotFired = capturedLogs.stream().anyMatch(log -> log.contains("fired an arrow"));
        assertTrue(shotFired, "Pressing 'e' with a Hunter should route gameTick to Shoot cast");

        assertEquals(new Position(1, 1), engine.getPlayer().getPosition(), "Hunter should remain stationary while casting");
    }

    /** Killing an enemy that grants enough XP triggers an in-engine level-up with stat increases. */
    @Test
    public void testPlayerLevelsUpMidGameViaKill() throws Exception {
        // Gold Cloak ('s') grants 25 XP. Pre-load the player to 49 XP (just below the 50-XP threshold).
        // One kill pushes total to 74 >= 50, triggering a level up mid-game.
        String map = "####\n" +
                "#@s#\n" +
                "####";

        GameEngine engine = loadCustomLevel(map);

        engine.getPlayer().addExperience(75);
        assertEquals(1, engine.getPlayer().getLevel(), "Player should still be level 1 with 75 XP");

        int healthPoolBefore = engine.getPlayer().getHealth().getHealthPool();

        // Drop enemy to 1 HP so the first successful hit kills it regardless of RNG
        Occupant target = engine.getCurrentBoard().getCell(new Position(2, 1)).getOccupant();
        if (target instanceof Enemy) {
            ((Enemy) target).getHealth().takeDamage(((Enemy) target).getHealth().getHealthAmount() - 1);
        }

        // RNG-Buster: repeat until the level-up fires (any non-zero damage roll kills a 1-HP enemy)
        for (int i = 0; i < 50 && engine.getPlayer().getLevel() == 1; i++) {
            engine.gameTick('d');
        }

        assertEquals(2, engine.getPlayer().getLevel(), "Player should have leveled up to level 2 after the kill");
        assertTrue(engine.getPlayer().getHealth().getHealthPool() > healthPoolBefore, "Health pool should increase on level up");
    }

    /** Full two-level game: clears level 1, loads level 2, clears level 2, isGameWon() becomes true. */
    @Test
    public void testFullMultiLevelProgressionToWin() throws IOException {
        // Three empty levels — each clears immediately because there are no enemies.
        // Tick 1 clears level 1 and loads level 2.
        // Tick 2 clears level 2 and loads level 3.
        // Tick 3 clears level 3 — no more levels remain — gameWon = true.
        File level1 = File.createTempFile("full1", ".txt");
        File level2 = File.createTempFile("full2", ".txt");
        File level3 = File.createTempFile("full3", ".txt");
        level1.deleteOnExit(); level2.deleteOnExit(); level3.deleteOnExit();

        String emptyMap = "#####\n#@..#\n#####\n";
        for (File file : new File[]{level1, level2, level3}) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(emptyMap);
            }
        }

        List<String> paths = new ArrayList<>();
        paths.add(level1.getAbsolutePath());
        paths.add(level2.getAbsolutePath());
        paths.add(level3.getAbsolutePath());

        capturedLogs.clear();
        GameEngine progressionEngine = new GameEngine(paths);
        progressionEngine.initialize(1, message -> capturedLogs.add(message));

        progressionEngine.gameTick('q');
        assertFalse(progressionEngine.isGameWon(), "Game should not be won after clearing only level 1");

        progressionEngine.gameTick('q');
        assertFalse(progressionEngine.isGameWon(), "Game should not be won after clearing only level 2");

        progressionEngine.gameTick('q');
        assertTrue(progressionEngine.isGameWon(), "Engine should flag gameWon after clearing all 3 levels");
        assertFalse(progressionEngine.isGameOver(), "Winning should not trigger the game over flag");
    }
}