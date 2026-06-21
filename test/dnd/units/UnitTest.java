package dnd.units;

import dnd.board.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTest {

    private DummyPlayer player;
    private DummyEnemy enemy;

    @BeforeEach
    public void setUp() {
        // Player: Level 1, 100 Health, 20 Attack, 5 Defense
        player = new DummyPlayer("Hero", 100, 20, 5);
        player.setPosition(new Position(0, 0));

        // Enemy: 50 Health, 10 Attack, 10 Defense, 100 XP
        enemy = new DummyEnemy("Goblin", 50, 10, 10, 100);
        enemy.setPosition(new Position(0, 1));
    }

    // --- ENGAGE IN COMBAT TESTS ---

    @Test
    public void testCombatAttackerWins() {
        // Player Attack Roll = 20. Enemy Defense Roll = 10. Damage = 10.
        player.visit(enemy);

        assertEquals(40, enemy.getHealth().getHealthAmount(), "Defender should lose health equal to Attack - Defense");
        assertEquals(100, player.getHealth().getHealthAmount(), "Attacker should not lose health during their own attack");
    }

    @Test
    public void testCombatDefenderFullyBlocks() {
        // Enemy Attack Roll = 10. Player Defense Roll = 5. Damage = 5.
        enemy.visit(player);

        assertEquals(95, player.getHealth().getHealthAmount(), "Player should take damage from enemy attack");

        // Give the player massive defense and attack again
        DummyPlayer tank = new DummyPlayer("Tank", 100, 5, 50);
        enemy.visit(tank); // 10 Atk vs 50 Def = -40 damage

        assertEquals(100, tank.getHealth().getHealthAmount(), "Defender taking negative damage should safely floor to 0");
    }

    @Test
    public void testCombatDeathAndPositionSwap() {
        // Slime: 20 Health, 0 Def.
        // IMPORTANT: XP set to 40 to prevent leveling up during a movement test!
        DummyEnemy weakEnemy = new DummyEnemy("Slime", 20, 0, 0, 40);

        Position initialPlayerPos = player.getPosition(); // (0, 0)
        Position initialEnemyPos = new Position(5, 5);
        weakEnemy.setPosition(initialEnemyPos);

        // Player attacks and kills the slime
        player.visit(weakEnemy);

        // 1. Verify Death
        assertTrue(weakEnemy.getHealth().isDead(), "Enemy should be dead");
        assertEquals(0, weakEnemy.getHealth().getHealthAmount(), "Dead enemy health should be exactly 0");

        // 2. Verify Instant XP
        assertEquals(40, player.getExperience(), "Player SHOULD gain exactly 40 XP instantly via the visit method.");
        assertEquals(1, player.getLevel(), "Player should remain level 1.");

        // 3. Verify the OOP Corpse Swap
        assertEquals(initialEnemyPos, player.getPosition(), "Player should step into the dead enemy's coordinates.");
        assertEquals(initialPlayerPos, weakEnemy.getPosition(), "The dead corpse should be swapped back to the player's old tile so the GameEngine can delete it safely.");
    }

    // --- LEVEL UP SYSTEM TEST ---

    @Test
    public void testExperienceAndLevelUp() {
        // Player starts at Level 1. Math check:
        // Lvl 1 -> 2 costs 50 (200 left). Stats: HP+20, Atk+8, Def+2
        // Lvl 2 -> 3 costs 100 (100 left). Stats: HP+30, Atk+12, Def+3
        // Lvl 3 -> 4 costs 150 (Stops here, 100 XP remains)
        player.addExperience(250);

        assertEquals(3, player.getLevel(), "Player should level up to 3 based on 250 XP");
        assertEquals(100, player.getExperience(), "Player should have 100 experience points left after level up.");

        // Checking base stats accumulation
        assertEquals(150, player.getHealth().getHealthPool(), "Max health should be 100 + 20 + 30 = 150");
        assertEquals(40, player.getAttackPower(), "Attack should be 20 + 8 + 12 = 40");
        assertEquals(10, player.getDefensePower(), "Defense should be 5 + 2 + 3 = 10");

        // Ensure healing occurred
        assertEquals(150, player.getHealth().getHealthAmount(), "Player should be fully healed upon leveling up.");
    }
}