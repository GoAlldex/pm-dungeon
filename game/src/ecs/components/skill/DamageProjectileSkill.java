package ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import ecs.entities.Hero;
import graphic.Animation;
import java.util.logging.Logger;
import starter.Game;
import tools.Point;

public abstract class DamageProjectileSkill implements ISkillFunction {

    private String pathToTexturesOfProjectile;
    private float projectileSpeed;

    private float projectileRange;
    private Damage projectileDamage;
    private Point projectileHitboxSize;
    private boolean requiredLevel = false;
    private Logger damageLogger = Logger.getLogger(getClass().getSimpleName());
    private ITargetSelection selectionFunction;

    public DamageProjectileSkill(
            String pathToTexturesOfProjectile,
            float projectileSpeed,
            Damage projectileDamage,
            Point projectileHitboxSize,
            ITargetSelection selectionFunction,
            float projectileRange) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.projectileHitboxSize = projectileHitboxSize;
        this.selectionFunction = selectionFunction;
    }

    public void setPathToTexturesOfProjectile(String pathToTexturesOfProjectile) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
    }

    public void setProjectileSpeed(float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public void setProjectileRange(float projectileRange) {
        this.projectileRange = projectileRange;
    }

    public void setProjectileDamage(Damage projectileDamage) {
        this.projectileDamage = projectileDamage;
    }

    public void setProjectileHitboxSize(Point projectileHitboxSize) {
        this.projectileHitboxSize = projectileHitboxSize;
    }

    public void setSelectionFunction(ITargetSelection selectionFunction) {
        this.selectionFunction = selectionFunction;
    }

    @Override
    public void execute(Entity entity) {
        Hero hero = (Hero) entity;
        if (hero.getLevel() >= 3 && projectileDamage.damageType() == DamageType.MAGIC) {
            Entity projectile = new Entity();
            PositionComponent epc =
                    (PositionComponent)
                            entity.getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            new PositionComponent(projectile, epc.getPosition());

            Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
            new AnimationComponent(projectile, animation);

            Point aimedOn = selectionFunction.selectTargetPoint();
            Point targetPoint =
                    SkillTools.calculateLastPositionInRange(
                            epc.getPosition(), aimedOn, projectileRange);
            Point velocity =
                    SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
            VelocityComponent vc =
                    new VelocityComponent(projectile, velocity.x, velocity.y, animation, animation);
            new ProjectileComponent(projectile, epc.getPosition(), targetPoint);
            ICollide collide =
                    (a, b, from) -> {
                        if (b != entity) {
                            b.getComponent(HealthComponent.class)
                                    .ifPresent(
                                            hc -> {
                                                ((HealthComponent) hc).receiveHit(projectileDamage);
                                                Game.removeEntity(projectile);
                                            });
                        }
                    };

            new HitboxComponent(
                    projectile, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);

            hero.isOnAttack(true);
            requiredLevel = true;
        } else {
            requiredLevel = false;
            hero.isOnAttack(false);
            damageLogger.info("Dein Level ist zu niedrig!!! (" + getClass().getSimpleName() + ")");
        }

        if (projectileDamage.damageType() != DamageType.MAGIC
                && hero.getHp().getCurrentHealthpoints() >= 0) {
            Entity projectile = new Entity();
            PositionComponent epc =
                    (PositionComponent)
                            entity.getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            new PositionComponent(projectile, epc.getPosition());

            Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
            new AnimationComponent(projectile, animation);

            Point aimedOn = selectionFunction.selectTargetPoint();
            Point targetPoint =
                    SkillTools.calculateLastPositionInRange(
                            epc.getPosition(), aimedOn, projectileRange);
            Point velocity =
                    SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
            VelocityComponent vc =
                    new VelocityComponent(projectile, velocity.x, velocity.y, animation, animation);
            new ProjectileComponent(projectile, epc.getPosition(), targetPoint);
            ICollide collide =
                    (a, b, from) -> {
                        if (b != entity) {
                            b.getComponent(HealthComponent.class)
                                    .ifPresent(
                                            hc -> {
                                                ((HealthComponent) hc).receiveHit(projectileDamage);
                                                Game.removeEntity(projectile);
                                            });
                        }
                    };

            new HitboxComponent(
                    projectile, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
            hero.isOnAttack(true);
        }
    }

    /**
     * Ist erforderliches Level erreicht
     *
     * @return true, Skill wurde ausgeführt, false der Level war zu niedrig.
     */
    public boolean isRequiredLevel() {
        return requiredLevel;
    }
}
