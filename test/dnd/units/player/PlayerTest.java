package dnd.units.player;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.DummyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private DummyPlayer player;

    @BeforeEach
    public void setUp() {
        // Player: Level 1, 100 Health, 20 Attack, 5 Defense
        player = new DummyPlayer("Test Hero", 100, 20, 5);
        player.setPosition(new Position(0, 0));
    }

    // --- INITIALIZATION TESTS ---

    /** Verifies level=1, experience=0, and toString='@' on a freshly constructed player. */
    @Test
    public void testPlayerInitialization() {
        assertEquals(1, player.getLevel(), "Player should start at level 1");
        assertEquals(0, player.getExperience(), "Player should start with 0 experience");
        assertEquals("@", player.toString(), "Living player should render as '@'");
    }

    // --- COMBAT & MOVEMENT (THE SWAP BUG FIX) TESTS ---

    /** Non-lethal hit: player and enemy keep their positions; player gains 0 XP. */
    @Test
    public void testMeleeCombatNonLethalDoesNotSwapPositions() {
        // Create an enemy that can survive the hit. Player Attack (20) vs Enemy Defense (0) = 20 Damage.
        // Enemy Health: 50 -> 30 (Survives)
        DummyEnemy toughEnemy = new DummyEnemy("Knight", 50, 10, 0, 100);
        Position enemyPos = new Position(1, 1);
        toughEnemy.setPosition(enemyPos);
        Position playerPos = player.getPosition(); // (0, 0)

        // The Action
        player.visit(toughEnemy);

        // 1. Verify health
        assertEquals(30, toughEnemy.getHealth().getHealthAmount(), "Enemy should survive with 30 HP");
        assertFalse(toughEnemy.getHealth().isDead(), "Enemy should not be dead");

        // 2. Verify NO SWAP OCCURRED (This prevents the Ghost Enemy Bug!)
        assertEquals(playerPos, player.getPosition(), "Player should NOT move into the enemy's tile if the enemy survives");
        assertEquals(enemyPos, toughEnemy.getPosition(), "Enemy should NOT move if it survives the hit");

        // 3. Verify NO XP
        assertEquals(0, player.getExperience(), "Player should gain 0 XP for a non-lethal hit");
    }

    /** Lethal hit: player steps into enemy's tile and corpse is swapped to player's old tile. */
    @Test
    public void testMeleeCombatLethalTriggersCorpseSwapAndXp() {
        // Create an enemy that will die in one hit.
        // XP set to 40 to purely test XP transfer without triggering a level up rollover.
        DummyEnemy weakEnemy = new DummyEnemy("Slime", 10, 5, 0, 40);
        Position enemyPos = new Position(1, 1);
        weakEnemy.setPosition(enemyPos);
        Position playerPos = player.getPosition(); // (0, 0)

        // The Action
        player.visit(weakEnemy);

        // 1. Verify death
        assertTrue(weakEnemy.getHealth().isDead(), "Enemy should be dead");

        // 2. Verify THE CORPSE SWAP OCCURRED
        assertEquals(enemyPos, player.getPosition(), "Player should step into the dead enemy's coordinates");
        assertEquals(playerPos, weakEnemy.getPosition(), "The dead corpse should be swapped back to the player's old tile so the GameEngine can delete it safely");

        // 3. Verify Instant XP
        assertEquals(40, player.getExperience(), "Player should gain XP instantly mid-combat");
    }

    // --- LEVEL UP MATH TESTS ---

    /** Exactly 50 XP triggers a level-up: verifies base stat increases and full heal. */
    @Test
    public void testAddExperienceExactLevelUp() {
        // Level 1 -> 2 requires exactly 50 XP
        player.addExperience(50);

        assertEquals(2, player.getLevel(), "Player should be level 2");
        assertEquals(0, player.getExperience(), "Experience should roll over back to 0");

        // Verify Base Stats Growth:
        // HP: 100 + (10 * 2) = 120
        // Attack: 20 + (4 * 2) = 28
        // Defense: 5 + (1 * 2) = 7
        assertEquals(120, player.getHealth().getHealthPool(), "Health should increase by 10 * level");
        assertEquals(120, player.getHealth().getHealthAmount(), "Health should fully heal upon leveling up");
        assertEquals(28, player.getAttackPower(), "Attack should increase by 4 * level");
        assertEquals(7, player.getDefensePower(), "Defense should increase by 1 * level");
    }

    /** 170 XP causes two consecutive level-ups with correct leftover XP. */
    @Test
    public void testAddExperienceMassiveRollover() {
        // Player starts at Level 1 (0 XP)
        // Add 170 XP.
        // Lvl 1 -> 2 costs 50 (120 left).
        // Lvl 2 -> 3 costs 100 (20 left).
        // Lvl 3 -> 4 costs 150 (Not enough, stops at Level 3).
        player.addExperience(170);

        assertEquals(3, player.getLevel(), "Player should jump to level 3");
        assertEquals(20, player.getExperience(), "Player should have exactly 20 experience leftover");
    }

    // --- GAME ENGINE SAFETIES ---

    /** Visiting a Player from another Player always throws IllegalStateException. */
    @Test
    public void testFriendlyFireThrowsException() {
        DummyPlayer ally = new DummyPlayer("Ally", 100, 10, 10);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            player.visit(ally);
        });
    }

    /** Dead player renders as 'X' instead of '@'. */
    @Test
    public void testPlayerCorpseRenderingForGameOver() {
        assertEquals("@", player.toString(), "Living player is '@'");

        // Instantly kill the player
        player.getHealth().takeDamage(999);

        assertTrue(player.getHealth().isDead(), "Player should be dead");
        assertEquals("X", player.toString(), "Dead player must return 'X' so the GameBoard prints the Game Over state correctly");
    }
}