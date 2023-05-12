package ecs.components.skill;

import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import ecs.entities.Zombie;
import starter.Game;
import tools.Point;

import java.util.ArrayList;
import java.util.Random;

public class ZombieSkill implements ISkillFunction{
    private ArrayList<Zombie> zombies = new ArrayList<>();
    private int level;
    private float dmgZombie;
    private Entity hero;
    private boolean setup;

    public ZombieSkill(ArrayList<Zombie> zombies, int level, float dmgZombie, Entity hero, boolean setup){
        this.zombies = zombies;
        this.level = level;
        this.dmgZombie = dmgZombie;
        this.hero = hero;
        setSetup(setup);
    }
    @Override
    public void execute(Entity entity) {
        Entity zombieEntity = new Entity();
        PositionComponent pc =
            (PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent ZombieSkill")
                );
        new PositionComponent(zombieEntity, pc.getPosition());
        Random rnd = new Random();
        int rndInt;
        if (setup){
            rndInt = rnd.nextInt(8)+1;
        }else{
            rndInt = rnd.nextInt(3)+1;
        }
        for (int i = 0; i <= rndInt; i++) {
            Zombie zombie = new Zombie(level);
            AIComponent zombieComponent = new AIComponent(zombie);
            zombieComponent.setIdleAI(
                entity1 -> {
                    AITools.move(
                        zombie,
                        AITools.calculatePath(
                            zombie,
                            hero
                        )
                    );
                    AITools.calculatePathToRandomTileInRange(
                        zombie,
                        new Random().nextInt(5)+2
                    );
                }
            );
            zombieComponent.execute();
            zombies.add(zombie);
        }
    }

    public void setSetup(boolean setup){
        this.setup = setup;
    }

    public boolean getSetup(){
        return setup;
    }
}
