package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class BoomerangSkill extends DamageProjectileSkill {
    public BoomerangSkill(ITargetSelection targetSelection) {
        super(
            "skills/boomerang/",
            0.4f,
            new Damage(1, DamageType.PHYSICAL, null),
            new Point(10, 10),
            targetSelection,
            7f);
    }
}
