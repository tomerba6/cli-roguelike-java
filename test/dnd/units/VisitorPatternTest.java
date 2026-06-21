package dnd.units;

import dnd.board.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class VisitorPatternTest {

    private DummyPlayer player;
    private DummyEnemy enemy;

    @BeforeEach
    public void setUp() {
        // Player: 100 Health, 20 Attack, 5 Defense
        player = new DummyPlayer("Hero", 100, 20, 5);
        player.setPosition(new Position(0, 0));

        // Enemy: 50 Health, 10 Attack, 10 Defense, 100 XP
        enemy = new DummyEnemy("Goblin", 50, 10, 10, 100);
        enemy.setPosition(new Position(0, 1));
    }

    // --- LEVEL 2 DOUBLE DISPATCH TESTS (Occupant Visitor) ---

    @Test
    public void testEnemyAcceptsPlayerVisitor() {
        // Player attempts to step on the Enemy's tile.
        // Instead of player.visit(enemy), we test the actual engine flow:
        enemy.accept(player);

        // If the Visitor Pattern is wired correctly, enemy.accept(player) should have
        // called player.visit(this), which triggers engageInCombat.
        // Player Attack (20) - Enemy Defense (10) = 10 Damage.

        assertEquals(40, enemy.getHealth().getHealthAmount(),
                "Enemy should take damage, proving accept() correctly routed to visit(Enemy)");
    }

    @Test
    public void testPlayerAcceptsEnemyVisitor() {
        // Enemy attempts to step on the Player's tile.
        player.accept(enemy);

        // If wired correctly, player.accept(enemy) calls enemy.visit(this).
        // Enemy Attack (10) - Player Defense (5) = 5 Damage.

        assertEquals(95, player.getHealth().getHealthAmount(),
                "Player should take damage, proving accept() correctly routed to visit(Player)");
    }

    @Test
    public void testFriendlyFireIsPreventedByVisitorPattern() {
        // An enemy accidentally tries to step on another enemy's tile
        DummyEnemy enemy2 = new DummyEnemy("Orc", 100, 10, 10, 100);

        enemy2.accept(enemy); // Enemy visits Enemy2

        // Because of the Visitor pattern, enemy.visit(Enemy e) should do nothing.
        // Neither enemy should take any damage!
        assertEquals(100, enemy2.getHealth().getHealthAmount(), "Enemies should not damage each other");
        assertEquals(50, enemy.getHealth().getHealthAmount(), "Enemies should not damage each other");
    }
}