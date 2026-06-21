package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.DummyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnemyTest {

    private DummyEnemy enemy;
    private DummyPlayer player;
    private List<String> capturedLogs;

    @BeforeEach
    public void setUp() {
        // Enemy: 50 Health, 15 Attack, 5 Defense, 100 Experience
        enemy = new DummyEnemy("Orc", 50, 15, 5, 100);
        enemy.setPosition(new Position(1, 1));

        // Player: 100 Health, 10 Attack, 5 Defense
        player = new DummyPlayer("Hero", 100, 10, 5);
        player.setPosition(new Position(1, 2));

        // Capture logs to verify the "Game Over" printouts
        capturedLogs = new ArrayList<>();
        enemy.setMessageCallback(message -> capturedLogs.add(message));
        player.setMessageCallback(message -> capturedLogs.add(message));
    }

    // --- INITIALIZATION TESTS ---

    @Test
    public void testEnemyInitialization() {
        assertEquals("Orc", enemy.getName(), "Enemy name should be correctly assigned");
        assertEquals(100, enemy.getExperienceValue(), "Experience value should be exactly 100");
    }

    @Test
    public void testDescriptionFormatting() {
        String desc = enemy.description();
        assertTrue(desc.contains("Experience Value: 100"), "Description should append the experience value correctly");
        assertTrue(desc.contains("Orc"), "Description should contain the unit's name");
    }

    // --- DOUBLE DISPATCH (VISITOR) TESTS ---

    @Test
    public void testFriendlyFireIsIgnored() {
        // Create a second enemy to act as the target
        DummyEnemy friendlyEnemy = new DummyEnemy("Goblin", 30, 5, 0, 50);

        // Enemy visits Enemy (This happens if an enemy pathfinds into another enemy)
        enemy.visit(friendlyEnemy);

        // Verify neither took damage (No friendly fire!)
        assertEquals(50, enemy.getHealth().getHealthAmount(), "Attacking enemy should not lose health");
        assertEquals(30, friendlyEnemy.getHealth().getHealthAmount(), "Defending enemy should not lose health");
    }

    // --- COMBAT & GAME OVER TESTS ---

    @Test
    public void testCombatAgainstPlayerNonLethal() {
        // Enemy Attack (15) vs Player Defense (5) = 10 Damage.
        // Player Health: 100 -> 90.
        enemy.visit(player);

        assertEquals(90, player.getHealth().getHealthAmount(), "Player should take exactly 10 damage");
        assertFalse(player.getHealth().isDead(), "Player should survive the attack");

        // Verify the Game Over logs were NOT triggered
        assertFalse(capturedLogs.contains("You lost."), "Game Over message should not print if the player survives");
    }

    @Test
    public void testCombatAgainstPlayerLethalTriggersGameOverLogs() {
        // Lower the player's health so the enemy one-shots them
        player.getHealth().takeDamage(95); // Player now has 5 HP
        assertEquals(5, player.getHealth().getHealthAmount());

        // Enemy Attack (15) vs Player Defense (5) = 10 Damage.
        // Player Health: 5 -> 0. (Dead!)
        enemy.visit(player);

        // 1. Verify Death
        assertTrue(player.getHealth().isDead(), "Player should be dead");
        assertEquals(0, player.getHealth().getHealthAmount(), "Player health should safely floor to 0");

        // 2. Verify "Game Over" Logs
        assertTrue(capturedLogs.contains("Hero was killed by Orc."), "Should log the specific death message");
        assertTrue(capturedLogs.contains("You lost."), "Should log the exact 'You lost.' text required by the assignment");
    }
}