package ecs.entities;

import ecs.components.*;
import ecs.components.ai.AIComponent;

/**
 * The Monster. It's entity in the ECS. This class helps to setup the Monster with
 * all its components and attributes .
 */
public abstract class Monster extends Entity {

    protected int hp;
    protected long xp;
    protected int dmg;
    protected int dmgType;
    protected float[] speed = new float[2];
    protected PositionComponent position;
    protected String pathToIdleLeft;
    protected String pathToIdleRight;
    protected String pathToRunLeft;
    protected String pathToRunRight;
    protected AIComponent ai;

    public int getHp() {
        return this.hp;
    }

    public long getXp() {
        return this.xp;
    }

    public int getDmg() {
        return this.dmg;
    }

    public int getDmgTyoe() {
        return this.dmgType;
    }

    public float[] getSpeed() {
        return this.speed;
    }

    public PositionComponent getPosition() {
        return this.position;
    }

    public void setPosition(PositionComponent position) {
        this.position = position;
    }

    public AIComponent getAi() {
        return this.ai;
    }

}
