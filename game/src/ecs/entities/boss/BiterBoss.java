package ecs.entities.boss;

import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.skill.HealSkill;
import ecs.components.skill.Skill;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import graphic.Animation;
import java.util.*;
import java.util.logging.Logger;
import starter.Game;

/**
 * BiterBoss
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 22.05.2023
 */
public class BiterBoss extends Boss {

    // Attributen
    private static final String PATH_TO_TEXTUR = "monster/type1/boss/";
    private static final Animation deathAnimation =
            AnimationBuilder.buildAnimation(PATH_TO_TEXTUR + "death");
    private static final Animation hitAnimation =
            AnimationBuilder.buildAnimation(PATH_TO_TEXTUR + "hit");
    private int heroXP = 0;
    private int heroDMG = 0;
    private float multi = 0.3f;
    private long timerStart = System.currentTimeMillis();
    private HealSkill healSkill; // Skill 1

    /**
     * Konstruktor
     *
     * @param level Held aktuelle level
     */
    public BiterBoss(int level) {
        super(level);
        this.bossLogger = Logger.getLogger(getClass().getName());
        setup();
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        setupSkills();
        setupIIdleAI();
        setItem();
        this.dmg = Math.round(1 * (multi + ((float) getLevel() / 10) - 0.1f) + 1);
    }

    /** Initialisiere alle erforderlichen Variablen für BiterBoss */
    private void setup() {
        this.position = new PositionComponent(this);
        bossPosition(getPosition());
        onDeath();
        this.pathToIdleLeft = PATH_TO_TEXTUR + "idleLeft";
        this.pathToIdleRight = PATH_TO_TEXTUR + "idleRight";
        this.pathToRunLeft = PATH_TO_TEXTUR + "runLeft";
        this.pathToRunRight = PATH_TO_TEXTUR + "runRight";
        this.hp = new HealthComponent(this);
        this.hp.setMaximalHealthpoints(
                Math.round((35 * getLevel()) * (0.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setCurrentHealthpoints(
                Math.round((35 * getLevel()) * (0.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setDieAnimation(deathAnimation);
        this.hp.setGetHitAnimation(getEmptyAnimation());
        this.hp.setOnDeath(death);
        this.speed[0] = 0.22f;
        this.speed[1] = 0.22f;
        this.xp = Math.round((35 * getLevel()) * (1 + ((float) getLevel() / 10) - 1.0f));
        bossLogger.info(getClass().getSimpleName() + " wurde initialisiert!");
    }

    /** Initialisiere ein Random Item, wenn der Boss besiegt wurde. */
    private void setItem() {
        ItemDataGenerator dataGenerator = new ItemDataGenerator();
        int rnd = new Random().nextInt(dataGenerator.getAllItems().size());
        this.item = dataGenerator.getItem(rnd);
    }

    /** HitboxComponent */
    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> {
                    if (other instanceof Hero) {
                        onCollision(Game.getHero().isPresent(), other);
                    }
                },
                (you, other, direction) -> onCollision(false, other));
    }

    /**
     * @param collision true: wenn die collision betreten wurde, ansonsten false findet kein
     *     collision statt.
     */
    private void onCollision(boolean collision, Entity other) {
        if (collision) {
            bossLogger.info("biterBossCollisionEnter");
            if (other instanceof Hero) {
                if (this.hero == null) {
                    this.hero = (Hero) other;
                }
                fight = true;
            }
        } else {
            bossLogger.info("biterBossCollisionLeave");
            fight = false;
        }
    }

    // Override method's

    /** Monster strategy implementieren */
    @Override
    protected void setupIIdleAI() {
        int radius = new Random().nextInt(4) + 1;
        int check = new Random().nextInt(3) + 1;
        int pause = new Random().nextInt(8) + 1;
        AIComponent biter = new AIComponent(this);
        biter.setIdleAI(new PatrouilleWalk(radius, check, pause, PatrouilleWalk.MODE.RANDOM));
        biter.execute();
    }

    /** initialisiere skills */
    @Override
    protected void setupSkills() {
        int min = 5;
        int max = 10;
        int coolDown = new Random().nextInt(max - min + 1) + min;
        this.healSkill = new HealSkill();
        skill1 = new Skill(healSkill, coolDown, 0);
    }

    @Override
    public String information() {
        return getClass().getSimpleName()
                + " hat folgende Skills und Eigenschaften: "
                + "\nSkills: "
                + healSkill.getClass().getSimpleName()
                + "("
                + healSkill.getHealPercent()
                + "% Heilt sich der Boss.\n"
                + getClass().getSimpleName()
                + " macht "
                + getDmg()
                + " damage.\n"
                + getClass().getSimpleName()
                + " ist (x:"
                + speed[0]
                + ", y:"
                + speed[1]
                + ") schnell.";
    }

    private void attacke() {
        if (!skill1.isOnCoolDown()) {
            bossLogger.info("Skill: HealSkill aktiviert");
            skill1.execute(this);
            if (!healSkill.isFull) {
                hp.setCurrentHealthpoints(healSkill.getHeal());
            }
        } else {
            bossLogger.info("Damage: attacke....");
            heroDMG += dmg;
            bossLogger.info("Damage: " + heroDMG);
        }
        this.hero.getHp().receiveHit(new Damage(heroDMG, DamageType.PHYSICAL, this));
    }

    @Override
    public void update(Set<Entity> entities, int level) {
        fightHero();
    }

    private void fightHero() {
        long timerEnd = System.currentTimeMillis();
        if (fight) {
            long time = (timerEnd - timerStart) / (60 * 60);
            if (time == new Random().nextInt(3) + 1) {
                if (Game.getHero().isPresent()) {
                    attacke();
                }
                timerStart = System.currentTimeMillis();
            }
            // Hero attacke
            if (time == new Random().nextInt(3) + 1) {
                long heroDMG = hero.damage();
                bossLogger.info("Boss hp: " + hp.getCurrentHealthpoints());
                hp.setCurrentHealthpoints(hp.getCurrentHealthpoints() - (int) heroDMG);
                bossLogger.info(
                        "heroAttack war erfolgreich!! "
                                + "Boss hat ("
                                + heroDMG
                                + "damage's) bekommen!!\n"
                                + "Boss hp: "
                                + hp.getCurrentHealthpoints());
                timerStart = System.currentTimeMillis();
            }
        }
        if (!fight) {
            timerStart = System.currentTimeMillis();
        }
    }

    public void onDeath() {
        this.death =
                entity -> {
                    log.info(getClass().getSimpleName() + " died.");
                    heroXP = (int) xp;
                    bossLogger.info(
                            "BiterBoss wurde besiegt!\n" + "Hero hat " + heroXP + "xp bekommen!!!");
                    new XPComponent(hero).setXP(heroXP);
                    this.hero.getMc().generateManaPointToNextLevel();
                    dropItem = true;
                    dropItem();
                };
    }

    private void dropItem() {
        if (dropItem) {
            Game.addEntity(WorldItemBuilder.buildWorldItem(item, this.position.getPosition()));
            dropItem = !dropItem;
        }
    }
}
