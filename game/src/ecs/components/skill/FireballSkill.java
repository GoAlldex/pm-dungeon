package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class FireballSkill extends DamageProjectileSkill {
    private Damage damage = new Damage(5, DamageType.FIRE, null);
    public FireballSkill(ITargetSelection targetSelection) {
        super(
                "skills/fireball/fireBall_Down/",
                0.5f,
                null,
                new Point(10, 10),
                targetSelection,
                5f);
        setProjectileDamage(damage);
    }

    /**
     * @return Damage objekt zurückliefern
     */
    public Damage getDamage(){
        return damage;
    }
}
