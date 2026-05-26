package dnd.units;

import dnd.units.Health;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HealthTest {
    private Health health;

    @BeforeEach
    public void setUp() {
        health = new Health(100);
    }

    @Test
    public void testInitialization() {
        assertEquals(100, health.getHealthPool(), "Health pool should be 100");
        assertEquals(100, health.getHealthAmount(), "Spawn health should match pool");
        assertFalse(health.isDead(), "Entity should not be dead on spawn");
    }

    @Test
    public void testConstructorThrowsExceptionOnInvalidPool() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Health(-50);
        }, "Creating health with a negative pool should throw an exception");

        assertThrows(IllegalArgumentException.class, () -> {
            new Health(0);
        }, "Creating health with a 0 pool should throw an exception");
    }

    @Test
    public void testTakeDamage() {
        health.takeDamage(30);
        assertEquals(70, health.getHealthAmount(), "Health should decrease by 30");

        health.takeDamage(100);
        assertEquals(0, health.getHealthAmount(), "Health should not drop below 0");
        assertTrue(health.isDead(), "Entity should be dead at 0 health");
    }

    @Test
    public void testHeal() {
        health.takeDamage(50);
        health.heal(20);
        assertEquals(70, health.getHealthAmount(), "Health should increase by 20");

        health.heal(100);
        assertEquals(100, health.getHealthAmount(), "Health should not exceed pool");
    }

    @Test
    public void testAddHealthPool() {
        health.addHealthPool(50);
        assertEquals(150, health.getHealthPool(), "Max health should increase to 150");
        assertEquals(100, health.getHealthAmount(), "Current health should not jump up automatically");
    }

    @Test
    public void testToStringFormat() {
        assertEquals("100/100", health.toString(), "String format should be Current/Max");

        health.takeDamage(40);
        assertEquals("60/100", health.toString(), "String format should update correctly after damage");
    }

    @Test
    public void testZeroDamageAndHeal() {
        health.takeDamage(0);
        assertEquals(100, health.getHealthAmount(), "0 damage should not change health");

        health.takeDamage(50);
        health.heal(0);
        assertEquals(50, health.getHealthAmount(), "0 heal should not change health");
    }

    @Test
    public void testNegativeDamageThrowsException() {
        // We assert that calling takeDamage(-20) WILL throw an IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            health.takeDamage(-20);
        });

        // We can even verify the error message is correct!
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }

    @Test
    public void testNegativeHealThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            health.heal(-10);
        }, "Healing for a negative amount should throw an exception");
    }

    @Test
    public void testNegativePoolThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            health.addHealthPool(-50);
        }, "Adding a negative amount to the health pool should throw an exception");
    }
}
