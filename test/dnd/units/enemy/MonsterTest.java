package dnd.units.enemy;

import dnd.board.Position;
import dnd.units.DummyPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonsterTest {

    private Monster monster;
    private DummyPlayer player;

    @BeforeEach
    public void setUp() {
        // Spawning "The Mountain" with stats: 1000 Health, 60 Attack, 25 Defense.
        // XP: 500, Vision Range: 6.
        monster = new Monster("The Mountain", 1000, 60, 25, 500, 6, 'M');
        monster.setPosition(new Position(5, 5)); // Center the monster at (5, 5)

        // Generic Player for targeting
        player = new DummyPlayer("Target", 100, 10, 10);
    }

    // --- AI CHASE PATHFINDING TESTS ---

    /** Monster chases player left when |dx| > |dy| and dx > 0. */
    @Test
    public void testTakeTurnMovesLeftWhenPlayerIsToTheLeft() {
        // Player is at (2, 5).
        // dx = 5 - 2 = 3. dy = 5 - 5 = 0.
        // |dx| > |dy| (3 > 0). dx > 0, so move Left.
        player.setPosition(new Position(2, 5));

        Position target = monster.takeTurn(player);
        assertEquals(new Position(4, 5), target, "Monster should move Left (X - 1)");
    }

    /** Monster chases player right when |dx| > |dy| and dx < 0. */
    @Test
    public void testTakeTurnMovesRightWhenPlayerIsToTheRight() {
        // Player is at (8, 5).
        // dx = 5 - 8 = -3. dy = 5 - 5 = 0.
        // |dx| > |dy| (3 > 0). dx is not > 0, so move Right.
        player.setPosition(new Position(8, 5));

        Position target = monster.takeTurn(player);
        assertEquals(new Position(6, 5), target, "Monster should move Right (X + 1)");
    }

    /** Monster chases player up when |dy| >= |dx| and dy > 0. */
    @Test
    public void testTakeTurnMovesUpWhenPlayerIsAbove() {
        // Player is at (5, 2).
        // dx = 5 - 5 = 0. dy = 5 - 2 = 3.
        // |dx| > |dy| is false (0 > 3). dy > 0, so move Up.
        player.setPosition(new Position(5, 2));

        Position target = monster.takeTurn(player);
        assertEquals(new Position(5, 4), target, "Monster should move Up (Y - 1)");
    }

    /** Monster chases player down when |dy| >= |dx| and dy < 0. */
    @Test
    public void testTakeTurnMovesDownWhenPlayerIsBelow() {
        // Player is at (5, 8).
        // dx = 5 - 5 = 0. dy = 5 - 8 = -3.
        // |dx| > |dy| is false (0 > 3). dy is not > 0, so move Down.
        player.setPosition(new Position(5, 8));

        Position target = monster.takeTurn(player);
        assertEquals(new Position(5, 6), target, "Monster should move Down (Y + 1)");
    }

    /** Perfect diagonal (|dx|==|dy|) falls through to the vertical branch. */
    @Test
    public void testTakeTurnDiagonalTieBreakerPrefersVertical() {
        // Player is at (3, 3) (Perfect diagonal).
        // dx = 5 - 3 = 2. dy = 5 - 3 = 2.
        // |dx| > |dy| is false (2 > 2 is false).
        // Logic drops to the 'else' block. dy > 0 (2 > 0), so it moves Up.
        player.setPosition(new Position(3, 3));

        Position target = monster.takeTurn(player);
        assertEquals(new Position(5, 4), target, "On a perfect diagonal tie, the logic defaults to vertical movement (Up)");
    }

    // --- AI OUT OF RANGE TESTS ---

    /** Out-of-vision player triggers random roam: distance moved is at most 1 and never diagonal. */
    @Test
    public void testTakeTurnRoamsRandomlyWhenOutOfVision() {
        // Player is at (20, 20). Distance is ~21.2, which is > Vision Range (6).
        player.setPosition(new Position(20, 20));

        Position target = monster.takeTurn(player);

        // Because the movement is random, we can't test a specific coordinate.
        // However, we CAN assert that the monster only moved a maximum of 1 step!
        double distanceMoved = monster.getPosition().range(target);

        // The distance between (5, 5) and any adjacent tile is 1.0.
        // If it stayed still, the distance is 0.0.
        assertTrue(distanceMoved <= 1.0, "Random movement should never teleport the monster more than 1 tile");

        // We can also assert it didn't move diagonally (which would be 1.41)
        assertNotEquals(1.41, distanceMoved, 0.01, "Monster should not be able to move diagonally");
    }

    /** toString() returns the single-char display symbol assigned at construction. */
    @Test
    public void testToStringReturnsCorrectSymbol() {
        assertEquals("M", monster.toString(), "Monster toString() must return its specific display tile string");
    }
}