package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import starter.Game;
import tools.Point;
/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version 1.0
 */
public class SlimeSkill extends Slime {
    private static String pathToTextur = "skills/slime";

    public SlimeSkill(ITargetSelection targetSelection, int dmgSlime, Entity hero){
        super(
            pathToTextur,
            0.5f,
            new Damage(dmgSlime, DamageType.PHYSICAL, hero),
            new Point(10, 10),
            targetSelection
        );
    }

    public void remove(Entity entity) {
        Game.removeEntity(entity);
    }

    /**
     * @return Slime Entity
     */
    public Entity getEntity(){
        return getSlime();
    }
}
