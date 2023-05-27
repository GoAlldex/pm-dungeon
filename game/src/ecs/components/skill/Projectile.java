package ecs.components.skill;

abstract class Projectile {
    private float damage;
    private float trajectory;
    private float speed;
    private float range;

    public Projectile (float damage, float trajectory, float speed, float range){
        this.damage = damage;
        this.trajectory = trajectory;
        this.speed = speed;
        this.range = range;
    }

}
