package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import graphic.Animation;

public class Biter extends Monster {

    /** Entity with Components */
    public Biter(int level) {
        super();
        this.position = new PositionComponent(this);
        this.hp = Math.round(15*(1+(level/10)));
        this.xp = Math.round(10*(1+(level/10)));
        this.dmg = Math.round(2*(1+(level/10)));
        this.dmgType = 0;
        this.speed[0] = 0.1f;
        this.speed[1] = 0.1f;
        this.pathToIdleLeft = "monster/type1/idleLeft";
        this.pathToIdleRight = "monster/type1/idleRight";
        this.pathToRunLeft = "monster/type1/runLeft";
        this.pathToRunRight = "monster/type1/runRight";
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
