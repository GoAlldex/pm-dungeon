package ecs.entities.boss;

import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.skill.MeditationSkill;
import ecs.components.skill.Skill;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import starter.Game;

/**
 * OrcBoss
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 24.05.2023
 */
public class OrcBoss extends Boss {

    // attributen
    private final String pathToTextur = "monster/type5/boss/";
    private int heroXP = 0;
    private int heroDMG = 0;
    private float multi = 0.35f;
    private boolean isCollision = false;
    private MeditationSkill meditationSkill;

    /**
     * Konstruktor
     *
     * @param level Held aktuelle level
     */
    public OrcBoss(int level, Entity heroEntity) {
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
     * Initialisiere alle erforderlichen Variablen für BiterBoss
     *
     * @param entity Hero entity
     */
    private void setup(Entity entity) {
        this.position = new PositionComponent(this);
        bossPosition(getPosition());
        heroPosition(new PositionComponent(entity));
        this.hero = entity;
        this.pathToIdleLeft = pathToTextur + "idleLeft";
        this.pathToIdleRight = pathToTextur + "idleRight";
        this.pathToRunLeft = pathToTextur + "runLeft";
        this.pathToRunRight = pathToTextur + "runRight";
        this.hp = new HealthComponent(this);
        this.hp.setMaximalHealthpoints(
                Math.round((35 * getLevel()) * (0.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setCurrentHealthpoints(hp.getMaximalHealthpoints());
        this.hp.setDieAnimation(getEmptyAnimation());
        this.hp.setGetHitAnimation(getEmptyAnimation());
        this.speed[0] = 0.22f;
        this.speed[1] = 0.22f;
        this.dmg = Math.round(dmg * (multi + ((float) getLevel() / 10) - 0.1f));
        this.xp = Math.round((35 * getLevel()) * (1 + ((float) getLevel() / 10) - 0.1f));
        bossLogger.info(getClass().getSimpleName() + " wurde initialisiert!");
        bossLogger.info(getClass().getSimpleName() + " hat " + hp.getCurrentHealthpoints() + "hp");
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
                (you, other, direction) -> onCollision(true),
                (you, other, direction) -> onCollision(false));
    }

    /**
     * @param collision true: wenn die collision betreten wurde, ansonsten false findet kein
     *     collision statt.
     */
    private void onCollision(boolean collision) {
        if (collision) {
            bossLogger.info("biterBossCollisionEnter");
        } else {
            bossLogger.info("biterBossCollisionLeave");
        }
        this.isCollision = collision;
    }

    // Override method's

    /** Monster strategy implementieren */
    @Override
    protected void setupIIdleAI() {
        int radius = new Random().nextInt(2) + 1;
        int check = new Random().nextInt(4) + 1;
        int pause = new Random().nextInt(10) + 1;
        AIComponent biter = new AIComponent(this);
        biter.setIdleAI(new PatrouilleWalk(radius, check, pause, PatrouilleWalk.MODE.RANDOM));
        biter.execute();
    }

    /** initialisiere skills */
    @Override
    protected void setupSkills() {
        int min = 5;
        int max = 10;
        int cooldown = new Random().nextInt(max - min + 1) + min;
        this.meditationSkill = new MeditationSkill();
        skill1 = new Skill(meditationSkill, cooldown, 0);
    }

    @Override
    public String information() {
        return getClass().getSimpleName()
                + " hat folgende Skills und Eigenschaften: "
                + "\nSkills: "
                + meditationSkill.getClass().getSimpleName()
                + "(Shield wird "
                + meditationSkill.shield()
                + "hp sein!)\n"
                + getClass().getSimpleName()
                + " macht "
                + dmg
                + " damage.\n"
                + getClass().getSimpleName()
                + " ist (x:"
                + speed[0]
                + ", y:"
                + speed[1]
                + ") schnell.";
    }

    private static long changeHPMax = 0;
    private static long changeHPCurrent = 0;

    private void attacke() {
        if (!skill1.isOnCoolDown()) {
            bossLogger.info(
                    "Skill: " + meditationSkill.getClass().getSimpleName() + " wurde aktiviert!");
            skill1.execute(this);
            changeHPMax = hp.getMaximalHealthpoints();
            changeHPCurrent = hp.getCurrentHealthpoints();
            hp.setMaximalHealthpoints(meditationSkill.shield());
            hp.setCurrentHealthpoints(hp.getMaximalHealthpoints());
        } else {
            bossLogger.info("Boss(" + getClass().getSimpleName() + ") attack....");
            heroDMG += dmg;
        }
    }

    private long timerStart = System.currentTimeMillis();

    @Override
    public void update(Set<Entity> entities, int level) {
        long timerEnd = System.currentTimeMillis();
        if (isCollision) {
            long time = (timerEnd - timerStart) / (60 * 60);
            if (time == 1) {
                attacke();
                timerStart = System.currentTimeMillis();
            }

            // effect random zurücksetzen
            if (skill1.isOnCoolDown() && time == new Random().nextInt(10) + 1) {
                hp.setMaximalHealthpoints((int) changeHPMax);
                hp.setCurrentHealthpoints((int) changeHPCurrent);
            }

            // hero attacke
            if (time == new Random().nextInt(2) + 1) {
                Hero hero1 = (Hero) hero;
                long heroAttackDMG = hero1.damage();
                bossLogger.info(
                        getClass().getSimpleName() + " hat " + hp.getCurrentHealthpoints() + "hp!");
                hp.setCurrentHealthpoints(hp.getCurrentHealthpoints() - (int) heroAttackDMG);
                bossLogger.info(
                        "heroAttack war erfolgreich!! "
                                + getClass().getSimpleName()
                                + " hat ("
                                + hero1.damage()
                                + "damage's) bekommen!!\n"
                                + getClass().getSimpleName()
                                + " hat noch "
                                + hp.getCurrentHealthpoints()
                                + "hp");
                timerStart = System.currentTimeMillis();
            }
        }
        if (!isCollision) {
            timerStart = System.currentTimeMillis();
        }

        // abfrage ob Boss tod ist
        if (hp.getCurrentHealthpoints() <= 0) {
            heroXP = (int) xp;
            bossLogger.info(
                    getClass().getSimpleName()
                            + " wurde besiegt!\n"
                            + "Hero hat "
                            + heroXP
                            + "xp bekommen!!!");
            new XPComponent(hero).setXP(heroXP);
            Hero hero1 = (Hero) hero;
            hero1.setHp(heroDMG);
            hero1.getMc().generateManaPointToNextLevel();
            PositionComponent pc =
                    (PositionComponent)
                            getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            entities.add(WorldItemBuilder.buildWorldItem(item, pc.getPosition()));
            Game.removeEntity(this);
        }
    }
}
