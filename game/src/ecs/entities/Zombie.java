package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import graphic.Animation;

public class Zombie extends Monster {

    /** Entity with Components */
    public Zombie(int level) {
        super();
        this.position = new PositionComponent(this);
        this.hp = Math.round(25*(1+(level/10)));
        this.xp = Math.round(20*(1+(level/10)));
        this.dmg = Math.round(4*(1+(level/10)));
        this.dmgType = 0;
        this.speed[0] = 0.25f;
        this.speed[1] = 0.25f;
        this.pathToIdleLeft = "monster/type2/idleLeft";
        this.pathToIdleRight = "monster/type2/idleRight";
        this.pathToRunLeft = "monster/type2/runLeft";
        this.pathToRunRight = "monster/type2/runRight";
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
