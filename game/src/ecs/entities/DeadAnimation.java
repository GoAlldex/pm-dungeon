package ecs.entities;

import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import graphic.Animation;
import tools.Point;

public class DeadAnimation extends Entity {

    private PositionComponent position;
    private Animation dieAnimation;

    public DeadAnimation(Entity entity) {
        this.position = new PositionComponent(this, getPosition(entity));
        this.dieAnimation = checkEntity(entity);
        setupAnimationComponent();
    }

    private Point getPosition(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            return hero.getPosition().getPosition();
        } else if (entity instanceof Monster) {
            Monster monster = (Monster) entity;
            return monster.getPosition().getPosition();
        }
        return null;
    }

    private void setupAnimationComponent() {
        new AnimationComponent(this, this.dieAnimation);
    }

    private Animation checkEntity(Entity entity) {
        if (entity instanceof Hero) {
            Hero hero = (Hero) entity;
            return hero.getHp().getDieAnimation();
        } else if (entity instanceof Monster) {
            Monster monster = (Monster) entity;
            return monster.getHp().getDieAnimation();
        }
        return null;
    }
}
