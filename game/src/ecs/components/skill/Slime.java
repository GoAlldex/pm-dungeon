package ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.entities.Entity;
import graphic.Animation;
import tools.Point;

/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version 1.0
 */
public abstract class Slime implements ISkillFunction {

    private String pathToTexturesOfSlime;
    private float speed;
    private Damage slimeDamge;
    private Point slimePoint;
    private ITargetSelection selectionFunction;
    private Entity slime = new Entity();

    public Slime(
            String pathToTexturesOfSlime,
            float speed,
            Damage slimeDamge,
            Point slimePoint,
            ITargetSelection selectionFunction) {
        this.pathToTexturesOfSlime = pathToTexturesOfSlime;
        this.speed = speed;
        this.slimeDamge = slimeDamge;
        this.slimePoint = slimePoint;
        this.selectionFunction = selectionFunction;
    }

    /**
     * Implements the concrete skill of an entity
     *
     * @param entity which uses the skill
     */
    @Override
    public void execute(Entity entity) {
        PositionComponent positionComponentSlime =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "PositionComponent Slime"));
        new PositionComponent(slime, positionComponentSlime.getPosition());
        Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfSlime);
        new AnimationComponent(slime, animation);
        ICollide collide =
                (a, b, from) -> {
                    if (b != entity) {
                        b.getComponent(HealthComponent.class)
                                .ifPresent(hc -> ((HealthComponent) hc).receiveHit(slimeDamge));
                    }
                };
        new HitboxComponent(slime, new Point(0.25f, 0.25f), slimePoint, collide, null);
    }

    public Entity getSlime() {
        return slime;
    }

    public Damage getSlimeDamge() {
        return slimeDamge;
    }
}
