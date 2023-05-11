package ecs.entities.boss;

import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillTools;
import ecs.components.skill.ZombieSkill;
import ecs.entities.Entity;
import ecs.entities.Zombie;
import ecs.components.skill.SlimeSkill;
import starter.Game;
import tools.Constants;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private int heroXP = 0;
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
        this.hp = (Math.round( (45 * getLevel()) * (1.5f + ((float) getLevel() / 10) - 0.1f)));
        staticHPBalken = getHp();
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
                System.out.println("Zombie hp: " + zombie.getHp());
                new HitboxComponent(
                    zombie,
                    (you, other, direction) -> {
                        if (other != zombie && other == hero){
                           zombie.setHp(zombie.getHp() - (int)(getLevel() * 5.5f));
                            info("skill2");
                            System.out.println("Zombies hp nach attacke: " + zombie.getHp());

                            if (zombie.getHp() <= 0){
                                System.out.println("Zombie wurde besiegt!");
                                heroXP += zombie.getXp();
                                System.out.println("Hero XP sind jetzt: " + heroXP);
                                Game.removeEntity(zombie);
                            }
                        }
                    },
                    (you, other, direction) -> System.out.println("Leave Zombie from Boss klass")
                );
            }
        }
    }

    private void setupHitboxComponent() {
        System.out.println("ZombieBoss hp: " + getHp());
        new HitboxComponent(
            this,
            (you, other, direction) -> {
                if (other != this && other == hero){
                    int i = (int) (getHp() - (getLevel() * 2.5f));
                    this.hp = i;
                    System.out.println("ZombieBoss hp nach attacke: " + getHp());
                    if (getHp() <= 0){
                        System.out.println("Der ZombieBoss wurde besiegt!");
                        heroXP += getXp();
                        System.out.println("Hero XP sind jetzt: " + heroXP);
                        System.out.println("Hero hat " + heroDMG + " damage's bekommen.");
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
                    System.out.println("Skill 1 wird aktiviert!");
                    skill1.execute(hero);
                    setupAttack(6f, multi + .25f, 1);
                    hit = 0;
                    System.out.println("Skill 1 wurde aktiviert!");
                    info("skill1");
                    skillaktivierung = true;
                    return;
                }
                if (skillaktivierung && !skill2.isOnCoolDown()){
                    System.out.println("Skill 2 wird aktiviert!");
                    skill2.execute(hero);
                    setupAttack(6f, multi + 0.25f, 2);
                    hit = 0;
                    System.out.println("Skill 2 wurde aktiviert!");
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
            if (staticHPBalken / 2 < getHp()){
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
            System.out.println("Slime damage: " + schleimDMG);
            System.out.println("ZombieBoss hat " + hp +"hp");
            heroDMG += schleimDMG;
        } else if (skill.equalsIgnoreCase("skill2")) {
            System.out.println("Zombie damage: " + zombieDMG);
            System.out.println("ZombieBoss hat " + hp +"hp");
            heroDMG += zombieDMG;
        } else {
            System.out.println("Damage: " + dmg);
            System.out.println("ZombieBoss hat " + hp +"hp");
            heroDMG += dmg;
        }
    }
}
