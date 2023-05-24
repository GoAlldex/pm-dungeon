package ecs.entities.boss;

import dslToGame.AnimationBuilder;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillTools;
import ecs.components.skill.ZombieSkill;
import ecs.damage.Damage;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.Zombie;
import ecs.components.skill.SlimeSkill;
import starter.Game;
import tools.Constants;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static ecs.damage.DamageType.PHYSICAL;

/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version 1.0
 */
public class ZombieBoss extends Boss{

    //Variablen
    protected String pathToTextur = "monster/type2/boss/";
    private float schleimDMG = 0f;
    private float zombieDMG = 0.25f;
    private static int staticHPBalken;
    private int heroDMG = 0;
    private float multi = 0.30f;
    private SlimeSkill slimeSkill;
    private static boolean init = false;
    private ArrayList<Zombie> zombies = new ArrayList<>();

    /**
     * Konstruktor
     * @param level aktuelles Level
     * @param heroEntity Hero entity
     */
    public ZombieBoss(int level, Entity heroEntity) {
        super(level);
        this.position = new PositionComponent(this);
        bossPosition(getPosition());
        heroPosition(new PositionComponent(heroEntity));
        this.hero = heroEntity;
        this.pathToIdleLeft = pathToTextur + "idleLeft";
        this.pathToIdleRight = pathToTextur + "idleRight";
        this.pathToRunLeft = pathToTextur + "runLeft";
        this.pathToRunRight = pathToTextur + "runRight";
        setup();
        setupVelocityComponent();
        setupAnimationComponent();
        setupSkills();
        setupHitboxComponent();
        setupIIdleAI();
    }

    /**
     * Boss setup
     */
    private void setup() {
        this.position = new PositionComponent(this);
        this.hitAnimation = AnimationBuilder.buildAnimation("");
        this.dieAnimation = AnimationBuilder.buildAnimation("monster/type2/boss/death");
        this.hp = new HealthComponent(this, Math.round(45f*(1.5f+((float)getLevel()/10f)-0.1f)), this.death, this.hitAnimation, this.dieAnimation);
        staticHPBalken = this.hp.getMaximalHealthpoints();
        this.speed[0] = zombieDMG * 0.8f;
        this.speed[1] = zombieDMG * 0.8f;
        setupAttack(6, multi, 0);
        setupAttack(6, multi, 2);
        this.xp = Math.round((45 * getLevel()) * (1 + ((float) getLevel() /10) - 0.1f));
    }



    private void setupAttack(float dmg, float multi, int w){
        if (w == 0){
            this.dmg = Math.round(dmg * (multi + ((float) getLevel() /10) - 0.1f));
        }else if (w == 1){
            this.schleimDMG = Math.round(getDmg() * dmg * (multi + ((float) getLevel() / 10) - 0.3f));
        }else if (w == 2){
            this.zombieDMG = Math.round(getDmg() * (multi + ((float) getLevel() /10) - 0.1f));
        }
    }

    /**
     * Random Walk
     */
    @Override
    protected void setupIIdleAI() {
        if (!init){
            for (int i = 0; i < new Random().nextInt(1)+1; i++) {
                Zombie zombie = new Zombie(getLevel());
                AIComponent zombieComponent = new AIComponent(zombie);
                zombieComponent.setIdleAI(
                    entity -> {
                        AITools.move(
                            zombie,
                            AITools.calculatePath(
                                zombie,
                                this
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
                hitboxZombie();
            }
            init = true;
        }else {
            staticWalk();
        }
    }


    @Override
    protected void setupSkills() {
        slimeSkill = new SlimeSkill(
            SkillTools::getCursorPositionAsPoint,
            (int) schleimDMG,
            hero
        );
        skill1 = new Skill(
            slimeSkill,
            1
        );

        skill2 = new Skill(
            new ZombieSkill(zombies, getLevel(), zombieDMG, hero, false),
            5
        );
    }

    private void hitboxZombie(){
        for (Zombie zombie : zombies) {
            new HitboxComponent(
                zombie,
                (a, b, from) -> setupHitboxZombie(),
                (a, b, from) -> {}
            );
        }
    }

    private void setupHitboxZombie(){
        if (zombies != null){
            for (Zombie zombie : zombies) {
                log.info("Zombie HP: " + zombie.getHp().getCurrentHealthpoints());
                new HitboxComponent(
                    zombie,
                    (you, other, direction) -> {
                        if (other != zombie && other == hero){
                            Hero h = (Hero) hero;
                            zombie.getHp().receiveHit(new Damage(((int)(getLevel()*5.5f)), PHYSICAL, h));
                            info("Skill 2");
                            log.info("Zombies HP after attack: " + zombie.getHp().getCurrentHealthpoints());

                            if (zombie.getHp().getCurrentHealthpoints() <= 0){
                                log.info("Zombie died!");
                                h.getXP().addXP(zombie.getXp());
                                log.info("Hero get "+zombie.getXp()+" XP");
                                Game.removeEntity(zombie);
                            }
                        }
                    },
                    (you, other, direction) -> log.info("Leave Zombie from Boss klass")
                );
            }
        }
    }

    private void setupHitboxComponent() {
        log.info("ZombieBoss hp: " + getHp().getCurrentHealthpoints());
        new HitboxComponent(
            this,
            (you, other, direction) -> {
                if (other != this && other == hero){
                    Hero h = (Hero) hero;
                    this.hp.receiveHit(new Damage(((int)(getLevel() * 2.5f)), PHYSICAL, h));
                    log.info("Zombie Boss HP after attack: " + getHp().getCurrentHealthpoints());
                    if (hp.getCurrentHealthpoints() <= 0){
                        log.info("Zombie Boss died.");
                        h.getXP().addXP(getXp());
                        log.info("Hero get "+getXp()+" XP");
                        log.info("Hero get " + heroDMG + " DMG.");
                        Game.removeEntity(this);
                        Game.removeEntity(slimeSkill.getEntity());
                        if (zombies != null){
                            for (Zombie z : zombies) {
                                Game.removeEntity(z.getAI().getEntity());
                            }
                        }
                        zombies.clear();
                    }
                    hitbox();
                }
            },
            (you, other, direction) -> leave()
        );
    }

    private void leave(){
        staticWalk();
    }

    private void staticWalk() {
        int radius = 20;
        AIComponent component = new AIComponent(hero);
        component.setIdleAI(
            entity -> {
                AITools.calculatePathToRandomTileInRange(
                    this,
                    radius
                );
            }
        );
        component.execute();
    }
    private int hit = 0;
    private boolean skillaktivierung = false;
    private void hitbox(){
        //attack slime
        if (skill1 != null || skill2 != null) {
            assert skill1 != null;
            assert skill2 != null;
            if (hit == 2){
                if (!skill1.isOnCoolDown()){
                    log.info("Skill 1 active!");
                    skill1.execute(hero);
                    setupAttack(6f, multi + .25f, 1);
                    hit = 0;
                    log.info("Skill 1 active!");
                    info("skill1");
                    skillaktivierung = true;
                    return;
                }
                if (skillaktivierung && !skill2.isOnCoolDown()){
                    log.info("Skill 2 active!");
                    skill2.execute(hero);
                    setupAttack(6f, multi + 0.25f, 2);
                    hit = 0;
                    log.info("Skill 2  active!");
                    info("skill2");
                    hitboxZombie();
                    Game.removeEntity(slimeSkill.getEntity());
                }
            }else {
                if (skill1.isOnCoolDown()){
                    skill1.reduceCoolDown();
                    System.err.println("Skill 1 is in cool down!");
                }
                if (skill2.isOnCoolDown()){
                    skill2.reduceCoolDown();
                    System.err.println("Skill 2 is in cool down!");
                }
            }
            if (hit == 2){
                hit = 0;
            }
            if (staticHPBalken / 2 < getHp().getCurrentHealthpoints()){
                //setup2();
            }
        }
        if (hit < 2){
            setupAttack(6f, multi + 0.25f, 0);
            info("");
        }
        hit++;
    }

    private void setup2(){
        skill1 = null;
        skill2 = null;
        slimeSkill = null;
        slimeSkill = new SlimeSkill(
            SkillTools::getCursorPositionAsPoint,
            (int) schleimDMG,
            hero
        );
        skill1 = new Skill(
            slimeSkill,
            0
        );

        skill2 = new Skill(
            new ZombieSkill(zombies, getLevel(), zombieDMG, hero, true),
            0
        );
    }

    private void info(String skill){
        if (skill.equalsIgnoreCase("skill1")){
            log.info("Slime damage: " + schleimDMG);
            log.info("Zombie Boss has " + hp.getCurrentHealthpoints() +" HP");
            heroDMG += schleimDMG;
        } else if (skill.equalsIgnoreCase("skill2")) {
            log.info("Zombie damage: " + zombieDMG);
            log.info("Zombie Boss has " + hp.getCurrentHealthpoints() +" HP");
            heroDMG += zombieDMG;
        } else {
            log.info("Damage: " + dmg);
            log.info("Zombie Boss has " + hp.getCurrentHealthpoints() +" HP");
            heroDMG += dmg;
        }
    }
}
