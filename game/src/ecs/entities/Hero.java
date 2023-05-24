package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.items.ItemDataGenerator;
import ecs.systems.MyFormatter;
import graphic.Animation;
import tools.Constants;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;
import static ecs.damage.DamageType.PHYSICAL;
import starter.Game;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity {

    private final int fireballCoolDown = 5;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;
    private XPComponent xp;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";
    private Skill firstSkill;

    private InventoryComponent inventory;
    int cd = 30;
    private HealthComponent hp;
    private IOnDeathFunction death;
    private PositionComponent position;
    private Animation hitAnimation = AnimationBuilder.buildAnimation("knight/hit");
    private Animation dieAnimation = AnimationBuilder.buildAnimation("knight/death");
    private AnimationComponent heroAnimation;
    private Set<Monster> fightHandler  = new HashSet<>();
    private int hitSpeed;
    private int hitPause = 0;
    private int frameTime;
    private int melee;
    private static final Logger log = Logger.getLogger(Hero.class.getName());

    /**
     <b><span style="color: rgba(3,71,134,1);">Logger für den Helden</span></b><br>
     Loggen der Helden  Ereignisse in der Datei Hero.txt im Ordner Logs.<br>

     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_3
     @since 21.05.2023
     */
    public static void HeroLogs(){
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("logs/log_Hero.txt",true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new MyFormatter("Hero"));
            log.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new MyFormatter("Hero"));
        log.addHandler(consoleHandler);
        log.setUseParentHandlers(false);
    }

    /** Entity with Components */
    public Hero() {
        super();
        this.xp = new XPComponent(this);
        this.position = new PositionComponent(this);
        onDeath();
        this.hp = new HealthComponent(this, 200, this.death, this.hitAnimation, this.dieAnimation);
        this.melee = 100;
        this.hitSpeed = 1;
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        PlayableComponent pc = new PlayableComponent(this);
        setupFireballSkill();
        pc.setSkillSlot1(firstSkill);
        setDefaultItems();
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        this.heroAnimation = new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupFireballSkill() {
        firstSkill =
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> {
                    if(other instanceof Monster) {
                        fightHandler.add((Monster) other);
                    }
                },
                (you, other, direction) -> {
                    if(other instanceof Monster) {
                        fightHandler.remove(other);
                    }
                });
    }

    private void setDefaultItems() {
        this.inventory = new InventoryComponent(this, 9);
        ItemDataGenerator itm = new ItemDataGenerator();
        this.inventory.addItem(itm.getItem(0));
        this.inventory.addItem(itm.getItem(1));
    }

    @Override
    public void update() {
        fightMonster();
    }

    private void fightMonster() {
        if(this.getHp().getCurrentHealthpoints() <= 0) {
            this.fightHandler.clear();
        } else {
            if (!this.fightHandler.isEmpty()) {
                this.frameTime = this.hitSpeed * Constants.FRAME_RATE;
                if (this.hitPause >= this.frameTime) {
                    this.hitPause = 0;
                    for (Monster m : this.fightHandler) {
                        if(Game.getEntities().contains(m)) {
                            m.getHp().receiveHit(new Damage(this.melee, PHYSICAL, this));
                            log.info("Hero hits " + m.getClass().getSimpleName() + ": " + this.melee + " Hp");
                            if (m.getHp().getCurrentHealthpoints() <= 0) {
                                fightHandler.remove(m);
                            }
                        } else {
                            fightHandler.remove(m);
                        }
                    }
                } else {
                    this.hitPause++;
                }
            }
        }
    }
    @Override
    public InventoryComponent getInventory() {
        return this.inventory;
    }

    public void onDeath() {
        this.death = entity -> {
            log.info("Hero died.");
        };
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Position</span></b><br>
     Position des Helden im Dungeon Level.
     @return PositionComponent gibt die Position des Helden zurück
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public PositionComponent getPosition() {
        return position;
    }

    public void setPosition(PositionComponent position) {
        this.position = position;
    }

    public HealthComponent getHp() {
        return this.hp;
    }

    public void setMaxHp(int level) {
        this.hp.setMaximalHealthpoints(this.hp.getMaximalHealthpoints()*(level*1));
    }

    public void setMeleeDmg(int level) {
        this.melee = this.melee*(level*1);
    }

    public int getDmg() {
        return this.melee;
    }

    public XPComponent getXP() {
        return this.xp;
    }

    public AnimationComponent getHeroAnimation() {
        return heroAnimation;
    }

    public void setHeroAnimation(AnimationComponent heroAnimation) {
        this.heroAnimation = heroAnimation;
    }

}
