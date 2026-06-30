package dnd.units;

/**
 * Represents the health statistics of a game entity.
 * Encapsulates the logic for taking damage, healing, and bounds-checking
 * to ensure health never drops below 0 or exceeds the maximum pool.
 */
public class Health {
    private int healthPool;
    private int healthAmount;

    /**
     * Constructs a new Health component.
     * Entities naturally spawn at maximum health.
     *
     * @param healthPool The maximum health capacity.
     * @throws IllegalArgumentException if healthPool is 0 or negative.
     */
    public Health(int healthPool) {
        if (healthPool <= 0) {
            throw new IllegalArgumentException("Fatal Math Error: Starting health pool must be greater than 0 (" + healthPool + ")");
        }
        this.healthPool = healthPool;
        this.healthAmount = healthPool;
    }

    public int getHealthPool() {
        return healthPool;
    }

    public int getHealthAmount() {
        return healthAmount;
    }


    /**
     * Reduces current health by a specific amount, stopping at 0.
     *
     * @param damageAmount The amount of damage to subtract.
     * @throws IllegalArgumentException if damageAmount is negative.
     */
    public void takeDamage(int damageAmount) {
        if (damageAmount < 0) {
            throw new IllegalArgumentException("Fatal Math Error: damageAmount cannot be negative (" + damageAmount + ")");
        }
        this.healthAmount = Math.max(0, this.healthAmount - damageAmount);
    }

    /**
     * Increases current health by a specific amount, capped at the health pool.
     *
     * @param healAmount The amount of health to restore.
     * @throws IllegalArgumentException if healAmount is negative.
     */
    public void heal(int healAmount) {
        if (healAmount < 0) {
            throw new IllegalArgumentException("Fatal Math Error: healAmount cannot be negative (" + healAmount + ")");
        }
        this.healthAmount = Math.min(this.healthPool, this.healthAmount + healAmount);
    }

    /**
     * Increases the maximum health pool (used during leveling up).
     * Does NOT automatically heal the unit, just raises the ceiling.
     *
     * @param amount The amount to add to the maximum pool.
     * @throws IllegalArgumentException if amount is negative.
     */
    public void addHealthPool(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Fatal Math Error: Cannot reduce health pool with negative amount (" + amount + ")");
        }
        this.healthPool += amount;
    }

    /**
     * Checks if the entity has died.
     *
     * @return True if health is 0, false otherwise.
     */
    public boolean isDead() {
        return this.healthAmount == 0;
    }

    @Override
    public String toString() {
        return healthAmount + "/" + healthPool;
    }
}
