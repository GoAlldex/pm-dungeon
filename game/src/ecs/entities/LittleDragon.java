package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import graphic.Animation;

public class LittleDragon extends Monster {

    /** Entity with Components */
    public LittleDragon(int level) {
        super();
        this.position = new PositionComponent(this);
        this.hp = Math.round(40*(1+(level/10)));
        this.xp = Math.round(30*(1+(level/10)));
        this.dmg = Math.round(7*(1+(level/10)));
        this.dmgType = 0;
        this.speed[0] = 0.2f;
        this.speed[1] = 0.2f;
        this.pathToIdleLeft = "monster/type3/idleLeft";
        this.pathToIdleRight = "monster/type3/idleRight";
        this.pathToRunLeft = "monster/type3/runLeft";
        this.pathToRunRight = "monster/type3/runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        this.ai = new AIComponent(this);
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, this.speed[0], this.speed[1], moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

}
