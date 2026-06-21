package dnd.units.player;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.enemy.Enemy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MageTest {

    private Mage mage;

    @BeforeEach
    public void setUp() {
        // Spawning "Melisandre":
        // 100 Health, 5 Attack, 1 Defense.
        // 300 Mana Pool, 30 Mana Cost, 15 Spell Power, 3 Hits, 6 Range.
        mage = new Mage("Melisandre", 100, 5, 1, 300, 30, 15, 3, 6);
        mage.setPosition(new Position(0, 0));
    }

    // --- INITIALIZATION AND LEVELING TESTS ---

    @Test
    public void testInitialization() {
        assertEquals(75, mage.getCurrentMana(), "Mage should start with 1/4 of their max mana (300 / 4 = 75)");
    }

    @Test
    public void testMageLevelUpMath() {
        // Force a level up to Level 2 (requires 50 XP)
        mage.addExperience(50);

        // 1. Verify Normal Player Stats
        assertEquals(120, mage.getHealth().getHealthPool(), "Health pool: 100 + (10 * 2) = 120");
        assertEquals(13, mage.getAttackPower(), "Attack: 5 + (4 * 2) = 13");

        // 2. Verify Mage-Specific Stats
        // Mana Pool: 300 + (25 * 2) = 350
        assertEquals(350, mage.getManaPool(), "Mana pool should increase by 25 * level");

        // Spell Power: 15 + (10 * 2) = 35
        assertEquals(35, mage.getSpellPower(), "Spell power should increase by 10 * level");

        // Current Mana: 75 + (350 / 4) = 75 + 87 = 162
        assertEquals(162, mage.getCurrentMana(), "Current mana should heal by manaPool / 4");
    }

    // --- TIME TICK (MANA REGEN) TESTS ---

    @Test
    public void testOnGameTickRegeneratesMana() {
        // Drain mana manually by casting.
        // Cost (30). No more hacky compensation!
        // 75 - 30 = 45 mana remaining
        boolean success = mage.castAbility(new ArrayList<>(), mage);
        assertTrue(success, "Cast should succeed");
        assertEquals(45, mage.getCurrentMana(), "Mana should deduct exactly the cost (30)");

        // Tick at level 1 regenerates 1 * level = 1 mana
        mage.onGameTick();
        assertEquals(46, mage.getCurrentMana(), "Tick should regenerate 1 mana at Level 1");

        // Force level up to test scaling regeneration
        mage.addExperience(50); // Now level 2

        // Math Check:
        // New Mana Pool = 300 + (25 * 2) = 350
        // Heals by: 350 / 4 = 87
        // Current Mana = 46 + 87 = 133
        assertEquals(133, mage.getCurrentMana(), "Mana should heal based on rolling state, not starting state");

        mage.onGameTick();
        // Previous mana (133) + 2 from level 2 tick = 135
        assertEquals(135, mage.getCurrentMana(), "Tick should regenerate 2 mana at Level 2");
    }

    // --- ABILITY RESOURCE TESTS ---

    @Test
    public void testCastAbilityFailsGracefullyWithoutMana() {
        // Mage starts with 75 mana. Cost is 30.
        boolean firstCast = mage.castAbility(new ArrayList<>(), mage); // Mana -> 45
        assertTrue(firstCast);

        boolean secondCast = mage.castAbility(new ArrayList<>(), mage); // Mana -> 15
        assertTrue(secondCast);

        // Third cast should fail silently and return false!
        boolean thirdCast = mage.castAbility(new ArrayList<>(), mage);
        assertFalse(thirdCast, "Third cast should fail due to lack of mana");

        assertEquals(15, mage.getCurrentMana(), "Mana should not be deducted if the cast aborts");
    }

    // --- COMBAT MATH AND MULTI-HIT TESTS ---

    @Test
    public void testBlizzardDamageMath() {
        // DummyEnemy ALWAYS rolls max defense (5)
        DummyEnemy enemy = new DummyEnemy("Ice Zombie", 100, 0, 5, 50);
        enemy.setPosition(new Position(0, 2)); // Distance 2 is within the Mage's Range of 6

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(enemy);

        // Mage Spell Power is 15. Enemy Defense is 5.
        // Damage per hit = 15 - 5 = 10 damage.
        // Hits Count = 3. Total damage expected = 30.
        mage.castAbility(activeEnemies, mage);

        assertEquals(70, enemy.getHealth().getHealthAmount(), "Enemy should take (15 - 5) * 3 = 30 total damage");

        // Verifying the pure mana deduction
        assertEquals(45, mage.getCurrentMana(), "Casting should consume exactly 30 mana");
    }

    @Test
    public void testBlizzardFiltersDeadBodiesGracefully() {
        // DummyEnemy with only 10 Health. It will die on the first hit of the Blizzard.
        // Drops 50 XP to trigger an instant level up!
        DummyEnemy weakEnemy = new DummyEnemy("Fragile Skeleton", 10, 0, 0, 50);
        weakEnemy.setPosition(new Position(1, 1));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(weakEnemy);

        // Cast Blizzard (3 hits)
        boolean success = mage.castAbility(activeEnemies, mage);
        assertTrue(success, "Cast should succeed");

        // 1. Verify the enemy died
        assertTrue(weakEnemy.getHealth().isDead(), "The first hit should kill the enemy");

        // 2. Verify the enemy was NOT removed from the active loop
        assertEquals(1, activeEnemies.size(), "Dead enemy MUST be left in the list for the GameEngine's janitor sweep");

        // 3. Verify the Mage leveled up instantly mid-ability!
        assertEquals(2, mage.getLevel(), "Mage should have safely processed the kill and gained XP immediately");
    }

    @Test
    public void testBlizzardIgnoresOutOFRangeEnemies() {
        // Enemy is at position (10, 10). Distance is ~14.1, which is > Ability Range (6)
        DummyEnemy farEnemy = new DummyEnemy("Sniper", 100, 0, 0, 50);
        farEnemy.setPosition(new Position(10, 10));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(farEnemy);

        boolean success = mage.castAbility(activeEnemies, mage);
        assertTrue(success, "Cast is still technically successful even if no targets are found");

        // The ability should execute, consume mana, realize no one is in range, and silently end.
        assertEquals(45, mage.getCurrentMana(), "Mana is consumed even if no targets are hit");
        assertEquals(100, farEnemy.getHealth().getHealthAmount(), "Out of range enemies should take 0 damage");
    }
}