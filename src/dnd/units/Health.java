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
     */
    public Health(int healthPool) {
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
     */
    public void takeDamage(int damageAmount) {
        this.healthAmount = Math.max(0, this.healthAmount - damageAmount);
    }

    /**
     * Increases current health by a specific amount, capped at the health pool.
     *
     * @param healAmount The amount of health to restore.
     */
    public void heal(int healAmount) {
        this.healthAmount = Math.min(this.healthPool, this.healthAmount + healAmount);
    }

    /**
     * Increases the maximum health pool (used during leveling up).
     * Does NOT automatically heal the unit, just raises the ceiling.
     *
     * @param amount The amount to add to the maximum pool.
     */
    public void addHealthPool(int amount) {
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
