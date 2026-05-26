package dnd.units.enemy;

public class Trap extends Enemy {
    /**
     * Constructs a new Enemy.
     * Matches the Unit super-constructor and adds the experience reward.
     *
     * @param name
     * @param healthPool
     * @param attackPower
     * @param defensePower
     * @param experienceValue
     */
    public Trap(String name, int healthPool, int attackPower, int defensePower, int experienceValue) {
        super(name, healthPool, attackPower, defensePower, experienceValue);
    }
}
