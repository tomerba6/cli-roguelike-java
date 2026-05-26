package dnd.units.enemy;

import dnd.combat.HeroicUnit;
import dnd.units.player.Player;

import java.util.List;

public class Boss extends Monster implements HeroicUnit {
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
    public Boss(String name, int healthPool, int attackPower, int defensePower, int experienceValue) {
        super(name, healthPool, attackPower, defensePower, experienceValue);
    }

    @Override
    public void castAbility(List<Enemy> activeEnemies, Player player) {

    }
}
