package dnd.units.player;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.enemy.Enemy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WarriorTest {

    private Warrior warrior;

    @BeforeEach
    public void setUp() {
        // Spawn a level 1 Warrior: 100 Health, 30 Attack, 4 Defense, Cooldown: 3 ticks
        warrior = new Warrior("Jon Snow", 100, 30, 4, 3);
        warrior.setPosition(new Position(0, 0));
    }

    // --- LEVELING TESTS ---

    @Test
    public void testWarriorLevelUpMath() {
        // Force a level up (Level 1 -> 2 requires 50 XP)
        warrior.addExperience(50);

        assertEquals(2, warrior.getLevel(), "Warrior should be level 2");

        // Base Health (+10*lvl) + Warrior Health (+5*lvl) = +15*lvl. (Adds 30).
        assertEquals(130, warrior.getHealth().getHealthPool(), "Health pool should scale with Warrior bonuses");

        // Base Attack (+4*lvl) + Warrior Attack (+2*lvl) = +6*lvl. (Adds 12).
        assertEquals(42, warrior.getAttackPower(), "Attack should scale with Warrior bonuses");

        // Base Defense (+1*lvl) + Warrior Defense (+2*lvl) = +3*lvl. (Adds 6).
        assertEquals(10, warrior.getDefensePower(), "Defense should correctly accumulate the +3 per level gain");

        // Cooldown should be completely reset upon leveling up
        assertEquals(0, warrior.getRemainingCooldown(), "Cooldown should reset to 0 on level up");
    }

    // --- TICKING AND COOLDOWN TESTS ---

    @Test
    public void testOnGameTickReducesCooldown() {
        boolean success = warrior.castAbility(new ArrayList<>(), warrior);
        assertTrue(success, "Ability cast should succeed with no cooldown");

        // The exact cooldown is now used, thanks to the Engine's intelligent tick skipping
        assertEquals(3, warrior.getRemainingCooldown(), "Cooldown should start exactly at the max cooldown (3)");

        warrior.onGameTick();
        assertEquals(2, warrior.getRemainingCooldown(), "Tick should reduce cooldown by 1");

        warrior.onGameTick();
        warrior.onGameTick();
        assertEquals(0, warrior.getRemainingCooldown(), "Cooldown should drop to 0");

        warrior.onGameTick();
        assertEquals(0, warrior.getRemainingCooldown(), "Cooldown should never drop below 0");
    }

    @Test
    public void testCastAbilityOnCooldownSafelyAborts() {
        DummyEnemy enemy = new DummyEnemy("Orc", 50, 10, 5, 100);
        enemy.setPosition(new Position(1, 1));
        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(enemy);

        // 1. First cast works perfectly
        boolean firstCast = warrior.castAbility(activeEnemies, warrior);
        assertTrue(firstCast, "First cast should succeed");
        assertEquals(3, warrior.getRemainingCooldown(), "Cooldown is active");
        int enemyHealthAfterFirstHit = enemy.getHealth().getHealthAmount();

        // 2. Second cast immediately after should abort silently and return false
        boolean secondCast = warrior.castAbility(activeEnemies, warrior);
        assertFalse(secondCast, "Second cast should fail and return false due to cooldown");

        // 3. Verify state didn't change
        assertEquals(3, warrior.getRemainingCooldown(), "Cooldown should not reset if the cast was aborted");
        assertEquals(enemyHealthAfterFirstHit, enemy.getHealth().getHealthAmount(), "Enemy should not take damage if ability is on cooldown");
    }

    // --- ABILITY MATH TESTS ---

    @Test
    public void testAvengersShieldMath() {
        // Set up the Warrior to be slightly damaged so we can verify healing
        warrior.getHealth().takeDamage(50); // Health is now 50/100

        DummyEnemy enemy = new DummyEnemy("Orc", 50, 10, 5, 100);
        enemy.setPosition(new Position(1, 1)); // Range is approx 1.41 (valid)

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(enemy);

        // --- CAST THE ABILITY ---
        boolean success = warrior.castAbility(activeEnemies, warrior);
        assertTrue(success, "Ability cast should succeed");

        // 1. Verify Healing: Heals for 10 * defense (10 * 4 = 40). 50 + 40 = 90 Health.
        assertEquals(90, warrior.getHealth().getHealthAmount(), "Warrior should heal for 10 * defense");

        // 2. Verify Mitigated Damage: 10% of max health (10) MINUS Enemy Defense Roll (5) = 5 Damage.
        // Enemy goes from 50 -> 45 Health.
        assertEquals(45, enemy.getHealth().getHealthAmount(), "Enemy should take 10% of Max HP minus their defense roll");
    }

    @Test
    public void testAvengersShieldKillsEnemyAndGrantsInstantXp() {
        // 5 Health enemy. Shield deals 5 mitigated damage (10 Atk - 5 Def), killing it exactly.
        // XP is set to 40 to cleanly test the XP gain without triggering a Level Up rollover!
        DummyEnemy weakEnemy = new DummyEnemy("Goblin", 5, 10, 5, 40);
        weakEnemy.setPosition(new Position(0, 1));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(weakEnemy);

        boolean success = warrior.castAbility(activeEnemies, warrior);
        assertTrue(success, "Ability cast should succeed");

        // 1. Verify the enemy is dead
        assertTrue(weakEnemy.getHealth().isDead(), "Enemy should be killed by the shield");

        // 2. Verify Instant XP (Native OOP design)
        assertEquals(40, warrior.getExperience(), "Warrior SHOULD gain XP instantly upon killing the enemy via the ability.");
        assertEquals(1, warrior.getLevel(), "Warrior should remain level 1.");
    }
}