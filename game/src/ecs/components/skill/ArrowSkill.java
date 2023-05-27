package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;

public class ArrowSkill extends DamageProjectileSkill {
    public ArrowSkill(ITargetSelection targetSelection) {
        super(
            "skills/arrow/",
            0.2f,
            new Damage(1, DamageType.PHYSICAL, null),
            new Point(10, 10),
            targetSelection,
            5f);
    }
}

