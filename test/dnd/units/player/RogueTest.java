package dnd.units.player;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.enemy.Enemy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RogueTest {

    private Rogue rogue;

    @BeforeEach
    public void setUp() {
        // Spawning "Arya Stark": 150 Health, 40 Attack, 2 Defense. 20 Energy Cost.
        rogue = new Rogue("Arya Stark", 150, 40, 2, 20);
        rogue.setPosition(new Position(1, 1)); // Center her so we can test surrounding tiles
    }

    // --- INITIALIZATION AND LEVELING TESTS ---

    @Test
    public void testInitialization() {
        assertEquals(100, rogue.getCurrentEnergy(), "Rogue should start with exactly 100 max energy");
    }

    @Test
    public void testRogueLevelUpMath() {
        // Drain some energy to verify it refills on level up.
        // Pure Cost (20). No more hacky compensation!
        boolean success = rogue.castAbility(new ArrayList<>(), rogue);
        assertTrue(success, "Cast should succeed");
        assertEquals(80, rogue.getCurrentEnergy(), "Energy drops by exactly the cost (20)");

        // Force a level up to Level 2 (requires 50 XP)
        rogue.addExperience(50);

        // 1. Verify Normal Player Stats
        assertEquals(170, rogue.getHealth().getHealthPool(), "Health pool: 150 + (10 * 2) = 170");
        assertEquals(4, rogue.getDefensePower(), "Defense: 2 + (1 * 2) = 4");

        // 2. Verify Rogue-Specific Stats
        // Base Player Attack increases by (4 * 2) = 8
        // Rogue Bonus Attack increases by (3 * 2) = 6
        // Total Attack: 40 + 8 + 6 = 54
        assertEquals(54, rogue.getAttackPower(), "Attack should scale with both Player and Rogue bonuses");

        // Energy reset
        assertEquals(100, rogue.getCurrentEnergy(), "Energy should completely refill to 100 on level up");
    }

    // --- TIME TICK (ENERGY REGEN) TESTS ---

    @Test
    public void testOnGameTickRegeneratesEnergy() {
        // Drain energy manually by casting (100 -> 80)
        rogue.castAbility(new ArrayList<>(), rogue);
        assertEquals(80, rogue.getCurrentEnergy());

        // Tick regenerates +10 energy
        rogue.onGameTick();
        assertEquals(90, rogue.getCurrentEnergy(), "Tick should regenerate exactly 10 energy");

        rogue.onGameTick();
        assertEquals(100, rogue.getCurrentEnergy(), "Tick should regenerate exactly 10 energy");

        // Test the cap
        rogue.onGameTick();
        assertEquals(100, rogue.getCurrentEnergy(), "Energy should never exceed the cap of 100");
    }

    // --- ABILITY RESOURCE TESTS ---

    @Test
    public void testCastAbilityFailsGracefullyWithoutEnergy() {
        // Rogue starts with 100 energy. Pure cost is 20.
        // We can cast exactly 5 times before failing.
        assertTrue(rogue.castAbility(new ArrayList<>(), rogue)); // 100 -> 80
        assertTrue(rogue.castAbility(new ArrayList<>(), rogue)); // 80 -> 60
        assertTrue(rogue.castAbility(new ArrayList<>(), rogue)); // 60 -> 40
        assertTrue(rogue.castAbility(new ArrayList<>(), rogue)); // 40 -> 20
        assertTrue(rogue.castAbility(new ArrayList<>(), rogue)); // 20 -> 0

        assertEquals(0, rogue.getCurrentEnergy(), "Energy should be drained to 0");

        // 6th cast should fail silently and return false!
        boolean failedCast = rogue.castAbility(new ArrayList<>(), rogue);
        assertFalse(failedCast, "Cast should fail due to lack of energy");

        assertEquals(0, rogue.getCurrentEnergy(), "Energy should not be deducted if the cast aborts");
    }

    // --- COMBAT MATH AND MULTI-HIT TESTS ---

    @Test
    public void testFanOfKnivesHitsMultipleAdjacentTargets() {
        // Rogue is at (1, 1).
        // Enemy 1 at (0, 1) -> Distance 1.0 (Adjacent, Valid)
        DummyEnemy enemy1 = new DummyEnemy("Lannister Guard 1", 100, 0, 10, 50);
        enemy1.setPosition(new Position(0, 1));

        // Enemy 2 at (2, 2) -> Distance ~1.41 (Diagonal, Valid)
        DummyEnemy enemy2 = new DummyEnemy("Lannister Guard 2", 100, 0, 5, 50);
        enemy2.setPosition(new Position(2, 2));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(enemy1);
        activeEnemies.add(enemy2);

        // Cast Fan of Knives. Rogue Attack = 40.
        boolean success = rogue.castAbility(activeEnemies, rogue);
        assertTrue(success);

        // Enemy 1: 40 attack - 10 defense = 30 damage. (100 - 30 = 70 health)
        assertEquals(70, enemy1.getHealth().getHealthAmount(), "Enemy 1 should take flat attack minus defense");

        // Enemy 2: 40 attack - 5 defense = 35 damage. (100 - 35 = 65 health)
        assertEquals(65, enemy2.getHealth().getHealthAmount(), "Enemy 2 should take flat attack minus defense");

        assertEquals(80, rogue.getCurrentEnergy(), "Casting should consume exactly 20 energy");
    }

    @Test
    public void testFanOfKnivesIgnoresOutOFRangeEnemies() {
        // Rogue is at (1, 1).
        // Enemy at (1, 3) -> Distance is exactly 2.0 (Invalid, must be < 2)
        DummyEnemy farEnemy = new DummyEnemy("Archer", 100, 0, 0, 50);
        farEnemy.setPosition(new Position(1, 3));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(farEnemy);

        boolean success = rogue.castAbility(activeEnemies, rogue);
        assertTrue(success, "Cast is successful even if it hits no one");

        assertEquals(80, rogue.getCurrentEnergy(), "Energy is consumed even if no targets are hit");
        assertEquals(100, farEnemy.getHealth().getHealthAmount(), "Enemies at distance 2.0 or greater should take 0 damage");
    }

    @Test
    public void testFanOfKnivesKillsAndGrantsInstantXP() {
        // Weak enemy at adjacent tile (0, 0)
        // Set XP to 40 to cleanly test instantaneous XP gain without triggering a Level Up rollover!
        DummyEnemy weakEnemy = new DummyEnemy("Peasant", 10, 0, 0, 40);
        weakEnemy.setPosition(new Position(0, 0));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(weakEnemy);

        boolean success = rogue.castAbility(activeEnemies, rogue);
        assertTrue(success);

        assertTrue(weakEnemy.getHealth().isDead(), "The flat attack should kill the enemy");

        // Ghost Board check: Verify the corpse is left for the Engine's sweep
        assertEquals(1, activeEnemies.size(), "Dead enemy MUST be left in the list for the GameEngine's janitor sweep");

        // Native OOP Instant XP
        assertEquals(40, rogue.getExperience(), "Rogue should gain XP instantly mid-ability");
        assertEquals(1, rogue.getLevel(), "Rogue should remain level 1");
    }
}