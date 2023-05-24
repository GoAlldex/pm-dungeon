package ecs.entities.boss;

import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.skill.HealSkill;
import ecs.components.skill.Skill;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import starter.Game;
import java.util.*;
import java.util.logging.Logger;
/**
 *  BiterBoss
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 22.05.2023
 */
public class BiterBoss extends Boss{

    //Attributen
    private final String pathToTextur = "monster/type1/boss/";
    private int heroXP = 0;
    private int heroDMG = 0;
    private float multi = 0.3f;
    private boolean isCollision = false;
    private HealSkill healSkill; //Skill 1

    /**
     * Konstruktor
     * @param level Held aktuelle level
     */
    public BiterBoss(int level, Entity heroEntity) {
        super(level);
        this.bossLogger = Logger.getLogger(getClass().getName());
        setup(heroEntity);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupSkills();
        setupIIdleAI();
        setItem();
    }

    /**
     * Initialisiere alle erforderlichen Variablen
     * für BiterBoss
     * @param heroEntity  Hero entity
     */
    private void setup(Entity heroEntity){
        this.position = new PositionComponent(this);
        bossPosition(getPosition());
        heroPosition(new PositionComponent(heroEntity));
        this.hero = heroEntity;
        this.pathToIdleLeft = pathToTextur + "idleLeft";
        this.pathToIdleRight = pathToTextur + "idleRight";
        this.pathToRunLeft = pathToTextur + "runLeft";
        this.pathToRunRight = pathToTextur + "runRight";
        this.hp = new HealthComponent(this);
        this.hp.setMaximalHealthpoints(Math.round((35 * getLevel()) * (0.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setCurrentHealthpoints(Math.round((35 * getLevel()) * (0.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setDieAnimation(getEmptyAnimation());
        this.hp.setGetHitAnimation(getEmptyAnimation());
        this.speed[0] = 0.22f;
        this.speed[1] = 0.22f;
        this.dmg = Math.round(dmg * (multi + ((float) getLevel() / 10) - 0.1f));
        this.xp = Math.round((35 * getLevel()) * (1 + ((float) getLevel() / 10) - 1.0f));
        bossLogger.info("BiterBoss wurde initialisiert!");
    }

    /**
     * Initialisiere ein Random Item, wenn der Boss besiegt wurde.
     */
    private void setItem(){
        ItemDataGenerator dataGenerator = new ItemDataGenerator();
        int rnd = new Random().nextInt(dataGenerator.getAllItems().size());
        this.item = dataGenerator.getItem(rnd);
    }

    /**
     * HitboxComponent
     */
    private void setupHitboxComponent(){
        new HitboxComponent(
          this,
            (you, other, direction) -> onCollision(true),
            (you, other, direction) -> onCollision(false)
        );
    }

    /**
     * @param collision true: wenn die collision betreten wurde, ansonsten false
     *                  findet kein collision statt.
     */
    private void onCollision(boolean collision){
        if (collision){
            bossLogger.info("biterBossCollisionEnter");
        }else {
            bossLogger.info("biterBossCollisionLeave");
        }
        this.isCollision = collision;
    }


    //Override method's

    /**
     * Monster strategy implementieren
     */
    @Override
    protected void setupIIdleAI() {
        int radius = new Random().nextInt(4)+1;
        int check = new Random().nextInt(3)+1;
        int pause = new Random().nextInt(8)+1;
        AIComponent biter = new AIComponent(this);
        biter.setIdleAI(
            new PatrouilleWalk(
                radius,
                check,
                pause,
                PatrouilleWalk.MODE.RANDOM
            )
        );
        biter.execute();
    }

    /**
     * initialisiere skills
     */
    @Override
    protected void setupSkills() {
        int min = 5;
        int max = 10;
        int coolDown = new Random().nextInt(max - min + 1) + min;
        this.healSkill = new HealSkill();
        skill1 =
            new Skill(
                healSkill,
                coolDown,
                0
            );
    }

    @Override
    public String information() {
        return getClass().getSimpleName() + " hat folgende Skills und Eigenschaften: "
            + "\nSkills: " + healSkill.getClass().getSimpleName() +
            "("+ healSkill.getHealPercent() + "% Heilt sich der Boss.\n"
            + getClass().getSimpleName() + " macht " + dmg + " damage.\n"
            + getClass().getSimpleName() + " ist (x:" + speed[0] + ", y:" + speed[1]
            + ") schnell."
            ;
    }

    private void attacke(){
        if (!skill1.isOnCoolDown()){
            bossLogger.info("Skill: HealSkill aktiviert");
            skill1.execute(this);
            if (!healSkill.isFull){
                hp.setCurrentHealthpoints(healSkill.getHeal());
            }
        }else{
            bossLogger.info("Damage: attacke....");
            heroDMG += dmg;
        }
    }

    long timerStart = System.currentTimeMillis();
    @Override
    public void update(Set<Entity> entities, int level) {
        long timerEnd = System.currentTimeMillis();
        if (isCollision){
            long time = (timerEnd - timerStart) / (60*60);
            if (time == new Random().nextInt(3)+1){
                attacke();
                timerStart = System.currentTimeMillis();
            }
            //Hero attacke
            if (time == new Random().nextInt(3)+1){
                Hero hero1 = (Hero) hero;
                long heroDMG = hero1.damage();
                bossLogger.info("Boss hp: " + hp.getCurrentHealthpoints());
                hp.setCurrentHealthpoints(hp.getCurrentHealthpoints() - (int)heroDMG);
                bossLogger.info("heroAttack war erfolgreich!! " +
                    "Boss hat (" + heroDMG + "damage's) bekommen!!\n" +
                    "Boss hp: " + hp.getCurrentHealthpoints());
                timerStart = System.currentTimeMillis();
            }
        }
        if (!isCollision){
            timerStart = System.currentTimeMillis();
        }

        if (hp.getCurrentHealthpoints() <= 0){
            heroXP = (int)xp;
            bossLogger.info("BiterBoss wurde besiegt!\n" +
                "Hero hat " + heroXP + "xp bekommen!!!");
            new XPComponent(hero).setXP(heroXP);
            Hero hero1 = (Hero) hero;
            hero1.setHp(heroDMG);
            hero1.getMc().generateManaPointToNextLevel();
            PositionComponent pc
                = (PositionComponent) getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent")
                );
            entities.add(WorldItemBuilder.buildWorldItem(item, pc.getPosition()));
            Game.removeEntity(this);
        }
    }
}
