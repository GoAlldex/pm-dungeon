package ecs.entities.boss;

import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.skill.*;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.Zombie;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import graphic.Animation;
import java.util.*;
import java.util.logging.Logger;
import starter.Game;

/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version 1.0
 */
public class ZombieBoss extends Boss {

    // Variablen
    protected String pathToTextur = "monster/type2/boss/";
    private float schleimDMG = 0f;
    private float zombieDMG = 0.25f;
    private static int staticHPBalken;
    private int heroXP = 0;
    private int heroDMG = 0;
    private float multi = 0.5f;
    private SlimeSkill slimeSkill;
    private static boolean init = false;
    private ArrayList<Zombie> zombies = new ArrayList<>();
    private static final Animation DIE_ANIMATION =
            AnimationBuilder.buildAnimation("monster/type2/boss/death");

    /**
     * Konstruktor
     *
     * @param level aktuelles Level
     */
    public ZombieBoss(int level) {
        super(level);
        this.bossLogger = Logger.getLogger(getClass().getName());
        setItem();
        onDeath();
        setup();
        if (Game.getHero().isPresent()) {
            hero = (Hero) Game.getHero().get();
        }
        this.pathToIdleLeft = pathToTextur + "idleLeft";
        this.pathToIdleRight = pathToTextur + "idleRight";
        this.pathToRunLeft = pathToTextur + "runLeft";
        this.pathToRunRight = pathToTextur + "runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        setupSkills();
        setupHitboxComponent();
        setupIIdleAI();
    }

    /** Boss setup */
    private void setup() {
        this.position = new PositionComponent(this);
        this.hp = new HealthComponent(this);
        this.hp.setMaximalHealthpoints(100);
        this.hp.setCurrentHealthpoints(
                Math.round((45 * getLevel()) * (1.5f + ((float) getLevel() / 10) - 0.1f)));
        this.hp.setDieAnimation(DIE_ANIMATION);
        this.hp.setGetHitAnimation(getEmptyAnimation());
        this.hp.setOnDeath(death);
        staticHPBalken = getHp().getMaximalHealthpoints();
        this.speed[0] = zombieDMG * 0.8f;
        this.speed[1] = zombieDMG * 0.8f;
        setupAttack(1, multi, 0);
        setupAttack(1, multi, 2);
        this.xp = Math.round((45 * getLevel()) * (1 + ((float) getLevel() / 10) - 0.1f));
    }

    private void setupAttack(float dmg, float multi, int w) {
        if (w == 0) {
            this.dmg = Math.round(dmg * (multi + ((float) getLevel() / 10) - 0.1f));
        } else if (w == 1) {
            this.schleimDMG =
                    Math.round(getDmg() * dmg * (multi + ((float) getLevel() / 10) - 0.3f));
        } else if (w == 2) {
            this.zombieDMG = Math.round(getDmg() * (multi + ((float) getLevel() / 10) - 0.1f));
        }
    }

    /** Random Walk */
    @Override
    protected void setupIIdleAI() {
        if (!init) {
            for (int i = 0; i < new Random().nextInt(1) + 1; i++) {
                Zombie zombie = new Zombie(getLevel());
                AIComponent zombieComponent = new AIComponent(zombie);
                zombieComponent.setIdleAI(
                        entity -> {
                            AITools.move(zombie, AITools.calculatePath(zombie, this));
                            AITools.calculatePathToRandomTileInRange(
                                    zombie, new Random().nextInt(5) + 2);
                        });
                zombieComponent.execute();
                zombies.add(zombie);
            }
            init = true;
        } else {
            staticWalk();
        }
    }

    @Override
    protected void setupSkills() {
        slimeSkill = new SlimeSkill(SkillTools::getCursorPositionAsPoint, (int) schleimDMG, hero);
        skill1 = new Skill(slimeSkill, 1, 0);

        skill2 = new Skill(new ZombieSkill(zombies, getLevel(), zombieDMG, hero, false), 5, 0);
    }

    @Override
    public String information() {
        return getClass().getSimpleName()
                + " hat folgende Skills und Eigenschaften: "
                + "\nSkills: "
                + slimeSkill.getClass().getSimpleName()
                + "(DamageTyp: "
                + slimeSkill.getSlimeDamge().damageType()
                + "Damage amount: "
                + slimeSkill.getSlimeDamge().damageAmount()
                + ")\n"
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

    private void hitboxZombie() {
        for (Zombie zombie : zombies) {
            if (zombie.getHp().getCurrentHealthpoints() <= 0) {
                heroXP = (int) zombie.getXp();
            }
        }
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> {
                    if (other instanceof Hero) {
                        onCollision(true);
                        hero = (Hero) other;
                    }
                },
                (you, other, direction) -> onCollision(false));
    }

    private boolean collision;

    private void onCollision(boolean collision) {
        this.collision = collision;
    }

    private void leave() {
        staticWalk();
    }

    private void staticWalk() {
        int radius = 20;
        AIComponent component = new AIComponent(hero);
        component.setIdleAI(
                entity -> {
                    AITools.calculatePathToRandomTileInRange(this, radius);
                });
        component.execute();
    }

    private int hit = 0;
    private boolean skillaktivierung = false;

    private void hitbox() {
        // attack slime
        if (skill1 != null || skill2 != null) {
            assert skill1 != null;
            assert skill2 != null;
            if (hit == 2) {
                if (!skill1.isOnCoolDown()) {
                    skill1.execute(hero);
                    setupAttack(1f, multi + .25f, 1);
                    hit = 0;
                    bossLogger.info("Skill 1 wurde aktiviert");
                    info("skill1");
                    skillaktivierung = true;
                    return;
                }
                if (skillaktivierung && !skill2.isOnCoolDown()) {
                    skill2.execute(hero);
                    setupAttack(1f, multi + 0.25f, 2);
                    hit = 0;
                    bossLogger.info("Skill 2 wurde aktiviert");
                    info("skill2");
                }
            } else {
                if (skill1.isOnCoolDown()) {
                    skill1.reduceCoolDown();
                    bossLogger.info("cool down Skill 1!");
                }
                if (skill2.isOnCoolDown()) {
                    skill2.reduceCoolDown();
                    bossLogger.info("cool down Skill 2!");
                }
            }
            if (hit == 2) {
                hit = 0;
            }
        }
        if (hit < 2) {
            setupAttack(8f, multi + 0.25f, 0);
            info("");
        }
        hit++;
    }

    private void setup2() {
        skill1 = null;
        skill2 = null;
        slimeSkill = null;
        slimeSkill = new SlimeSkill(SkillTools::getCursorPositionAsPoint, (int) schleimDMG, hero);
        skill1 = new Skill(slimeSkill, 0, 0);

        skill2 = new Skill(new ZombieSkill(zombies, getLevel(), zombieDMG, hero, true), 0, 0);
    }

    private void info(String skill) {
        if (skill.equalsIgnoreCase("skill1")) {
            bossLogger.info("Slime damage: " + schleimDMG);
            bossLogger.info("ZombieBoss hat " + hp + "hp");
            heroDMG += schleimDMG;
        } else if (skill.equalsIgnoreCase("skill2")) {
            bossLogger.info("Zombie damage: " + zombieDMG);
            bossLogger.info("ZombieBoss hat " + hp + "hp");
            System.out.println();
            System.out.println();
        } else {
            bossLogger.info("Damage: " + dmg);
            bossLogger.info("ZombieBoss hat " + hp + "hp");
            heroDMG += dmg;
        }
    }

    private long timerStart = System.currentTimeMillis();

    @Override
    public void update(Set<Entity> entities, int level) {
        long timerEnd = System.currentTimeMillis();
        if (getHp().getMaximalHealthpoints() >= 0) {
            if (collision) {
                if ((timerEnd - timerStart) / (60 * 60) == 1) {
                    attack();
                    timerStart = System.currentTimeMillis();
                }
            }
            if (!collision) {
                timerStart = System.currentTimeMillis();
            }
        }
        hitboxZombie();
    }

    private void attack() {
        if (hero.getHp() != null) {
            bossLogger.info("ZombieBoss hat " + hero.damage() + " damage's bekommen!");
            this.hp.receiveHit(
                    new Damage(
                            getHp().getCurrentHealthpoints() - getLevel() * (int) hero.damage(),
                            DamageType.PHYSICAL,
                            this));
        }
        hitbox();
    }

    public void onDeath() {
        this.death =
                entity -> {
                    dropItem = true;
                    dropItem();
                    bossLogger.info("Der ZombieBoss wurde besiegt!");
                    bossLogger.info("Hero XP sind jetzt: " + heroXP);
                    bossLogger.info("Hero hat " + heroDMG + " damage's bekommen.");
                    new XPComponent(hero).setXP(heroXP);
                };
    }

    /** Initialisiere ein Random Item, wenn der Boss besiegt wurde. */
    private void setItem() {
        ItemDataGenerator dataGenerator = new ItemDataGenerator();
        int rnd = new Random().nextInt(dataGenerator.getAllItems().size());
        this.item = dataGenerator.getItem(rnd);
    }

    private void dropItem() {
        if (dropItem) {
            Game.addEntity(WorldItemBuilder.buildWorldItem(item, this.position.getPosition()));
            dropItem = !dropItem;
        }
    }
}
