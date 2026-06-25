package dnd.units.player;

import dnd.board.Position;
import dnd.units.DummyEnemy;
import dnd.units.enemy.Enemy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HunterTest {

    private Hunter hunter;

    @BeforeEach
    public void setUp() {
        // Spawning "Ygritte" using the exact stats from the assignment table:
        // 220 Health, 30 Attack, 2 Defense. 6 Range.
        hunter = new Hunter("Ygritte", 220, 30, 2, 6);
        hunter.setPosition(new Position(0, 0));
    }

    // --- INITIALIZATION AND LEVELING TESTS ---

    /** Starting arrows = 10 * level (10 at level 1); ticksCount starts at 0. */
    @Test
    public void testInitialization() {
        // Level 1 Hunter starts with 10 * level arrows
        assertEquals(10, hunter.getArrowsCount(), "Hunter should start with exactly 10 arrows at Level 1");
        assertEquals(0, hunter.getTicksCount(), "Ticks count should start at 0");
    }

    /** Level-up to 2: verifies combined base+hunter attack, defense, and arrow count gains. */
    @Test
    public void testHunterLevelUpMath() {
        // Drain an arrow to verify cap applies cleanly on level-up
        List<Enemy> dummyTargetList = new ArrayList<>();
        DummyEnemy dummy = new DummyEnemy("Target", 1000, 0, 0, 50);
        dummy.setPosition(new Position(0, 1));
        dummyTargetList.add(dummy);

        boolean success = hunter.castAbility(dummyTargetList, hunter);
        assertTrue(success, "Cast should succeed");
        assertEquals(9, hunter.getArrowsCount());

        // Force a level up to Level 2 (requires 100 XP)
        hunter.addExperience(100);

        // 1. Verify Normal Player Stats (flat gains)
        assertEquals(235, hunter.getHealth().getHealthPool(), "Health pool: 220 + 15 = 235");

        // 2. Verify Hunter-Specific Stats
        // Base Player Attack increases by flat 5
        // Hunter Bonus Attack increases by flat 2
        // Total Attack: 30 + 5 + 2 = 37
        assertEquals(37, hunter.getAttackPower(), "Attack should scale with both Player and Hunter bonuses");

        // Base Player Defense increases by flat 1
        // Hunter Bonus Defense increases by flat 1
        // Total Defense: 2 + 1 + 1 = 4
        assertEquals(4, hunter.getDefensePower(), "Defense should scale with both Player and Hunter bonuses");

        // Arrows: capped at 10 * level = 20 at level 2
        assertEquals(20, hunter.getArrowsCount(), "Arrows should fill to the cap (10 * level) upon leveling up");
    }

    // --- TIME TICK (DELAYED ARROW REGEN) TESTS ---

    /** After exactly 11 ticks the regen fires: arrows increase by 1 (flat), capped at 10×level. */
    @Test
    public void testOnGameTickRegeneratesArrowsAfterDelay() {
        // Drain 1 arrow so regen can fire (cap is 10; regen does nothing if already at cap).
        DummyEnemy drainTarget = new DummyEnemy("Training Target", 1000, 0, 0, 50);
        drainTarget.setPosition(new Position(0, 1));
        List<Enemy> drainList = new ArrayList<>();
        drainList.add(drainTarget);
        hunter.castAbility(drainList, hunter);
        assertEquals(9, hunter.getArrowsCount(), "One arrow should be spent");

        // Tick 10 times. The counter should reach 10, but arrows should NOT increase yet.
        for (int i = 0; i < 10; i++) {
            hunter.onGameTick();
        }
        assertEquals(10, hunter.getTicksCount(), "Counter should be exactly 10");
        assertEquals(9, hunter.getArrowsCount(), "Arrows should not change before the regen tick");

        // Tick an 11th time. Condition (ticksCount == 10) is met: +1 arrow, capped at 10.
        hunter.onGameTick();
        assertEquals(0, hunter.getTicksCount(), "Counter should reset to 0");
        assertEquals(10, hunter.getArrowsCount(), "Regen adds exactly 1 arrow, capped at 10×level (10)");
    }

    /** ticksCount is not reset when the hunter levels up mid-regen cycle. */
    @Test
    public void testTicksCountCarriesAcrossLevelUp() {
        // Advance the tick counter partway through the regen cycle.
        for (int i = 0; i < 5; i++) {
            hunter.onGameTick();
        }
        assertEquals(5, hunter.getTicksCount(), "Ticks should be 5 before leveling up");

        // Force a level up (100 XP required at Level 1)
        hunter.addExperience(100);
        assertEquals(2, hunter.getLevel(), "Hunter should now be level 2");

        // The spec does not say to reset ticksCount on level up — it should carry over unchanged.
        assertEquals(5, hunter.getTicksCount(), "ticksCount should not be reset by a level up");
    }

    // --- ABILITY RESOURCE TESTS ---

    /** Casting with 0 arrows returns false; enemy takes no damage and arrow count stays at 0. */
    @Test
    public void testCastAbilityFailsGracefullyWithoutArrows() {
        // Create a valid target
        DummyEnemy validTarget = new DummyEnemy("Target", 1000, 0, 0, 50);
        validTarget.setPosition(new Position(0, 1));
        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(validTarget);

        // Hunter starts with 10 arrows. Cast 10 times to drain it to 0.
        for (int i = 0; i < 10; i++) {
            assertTrue(hunter.castAbility(activeEnemies, hunter), "Casts 1-10 should succeed");
        }
        assertEquals(0, hunter.getArrowsCount(), "Arrows should be completely drained");
        int healthBeforeAbort = validTarget.getHealth().getHealthAmount();

        // 11th cast should fail silently and return false!
        boolean failedCast = hunter.castAbility(activeEnemies, hunter);
        assertFalse(failedCast, "Cast should fail due to lack of arrows");

        assertEquals(0, hunter.getArrowsCount(), "Arrows should not drop below 0");
        assertEquals(healthBeforeAbort, validTarget.getHealth().getHealthAmount(), "Target should not take damage when quiver is empty");
    }

    /** With no enemy within range, cast returns false and no arrow is consumed. */
    @Test
    public void testCastAbilityFailsGracefullyIfNoEnemiesInRange() {
        // Enemy is at position (10, 10). Distance is ~14.1, which is > Ability Range (6)
        DummyEnemy farEnemy = new DummyEnemy("Wildling", 100, 0, 0, 50);
        farEnemy.setPosition(new Position(10, 10));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(farEnemy);

        // Cast should fail silently and return false!
        boolean failedCast = hunter.castAbility(activeEnemies, hunter);
        assertFalse(failedCast, "Cast should fail if no valid targets exist");

        assertEquals(10, hunter.getArrowsCount(), "Arrow should NOT be consumed if no valid targets exist");
        assertEquals(100, farEnemy.getHealth().getHealthAmount(), "Out of range enemy should take no damage");
    }

    // --- COMBAT TARGETING MATH TESTS ---

    /** Shoot targets the closest in-range enemy; other valid enemies take no damage. */
    @Test
    public void testShootTargetsClosestEnemyCorrectly() {
        // Hunter is at (0, 0)

        // Enemy 1: Distance 5.0 (Valid, but far)
        DummyEnemy farTarget = new DummyEnemy("Far Target", 100, 0, 5, 50);
        farTarget.setPosition(new Position(0, 5));

        // Enemy 2: Distance 2.0 (Valid, Closest!)
        DummyEnemy closestTarget = new DummyEnemy("Closest Target", 100, 0, 5, 50);
        closestTarget.setPosition(new Position(0, 2));

        // Enemy 3: Distance 3.0 (Valid, but in the middle)
        DummyEnemy middleTarget = new DummyEnemy("Middle Target", 100, 0, 5, 50);
        middleTarget.setPosition(new Position(0, 3));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(farTarget);
        activeEnemies.add(closestTarget);
        activeEnemies.add(middleTarget);

        // Cast Shoot. Hunter Attack = 30. Defense = 5. Damage should be 25.
        boolean success = hunter.castAbility(activeEnemies, hunter);
        assertTrue(success, "Cast should successfully fire at the closest target");

        // Assert that ONLY the closest target took damage
        assertEquals(100, farTarget.getHealth().getHealthAmount(), "Far target should take 0 damage");
        assertEquals(100, middleTarget.getHealth().getHealthAmount(), "Middle target should take 0 damage");
        assertEquals(75, closestTarget.getHealth().getHealthAmount(), "Only the closest target should take damage");

        assertEquals(9, hunter.getArrowsCount(), "Casting should consume exactly 1 arrow");
    }

    /** Enemy at exactly range=6 is a valid target; kill grants XP instantly. */
    @Test
    public void testShootKillsTargetAndGrantsInstantXP() {
        // Range is 6. Place a weak enemy at exactly distance 6.0 (e.g., coordinates 0, 6).
        // XP set to 40 to purely test XP transfer without triggering a level up rollover.
        DummyEnemy edgeTarget = new DummyEnemy("Edge Target", 10, 0, 0, 40);
        edgeTarget.setPosition(new Position(0, 6));

        List<Enemy> activeEnemies = new ArrayList<>();
        activeEnemies.add(edgeTarget);

        boolean success = hunter.castAbility(activeEnemies, hunter);
        assertTrue(success, "Cast should succeed");

        assertTrue(edgeTarget.getHealth().isDead(), "Target exactly on the edge of the range should be hit and killed");

        // Ghost Board check: Verify the corpse is left for the Engine's sweep
        assertEquals(1, activeEnemies.size(), "Dead target MUST be left in the active list for the GameEngine's janitor sweep");

        // Native OOP Instant XP
        assertEquals(40, hunter.getExperience(), "Hunter should gain XP instantly mid-ability");
        assertEquals(1, hunter.getLevel(), "Hunter should remain level 1");
    }
}