package ecs.entities;

import static ecs.damage.DamageType.PHYSICAL;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.components.xp.XPComponent;
import ecs.damage.Damage;
import ecs.entities.boss.Boss;
import ecs.items.ItemData;
import ecs.items.ItemDataGenerator;
import ecs.systems.MyFormatter;
import graphic.Animation;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;
import java.util.logging.Logger;

import graphic.hud.hud.HUD;
import graphic.hud.inventory.HeroGraphicInventory;
import starter.Game;
import tools.Constants;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity {

    private int fireballCoolDown = 2, lightningCoolDown = 0;
    private LightningLineSkill lightningLineSkill;
    private FireballSkill fireballSkill;
    private TransformSkill transformSkill;
    private MindControlSkill mindControlSkill;
    private float xSpeed = 0.3f;
    private float ySpeed = 0.3f;
    private long dmg;
    private final int arrowCoolDown = 1;
    private final int boomerangCoolDown = 2;

    private String pathToIdleLeft = "knight/idleLeft";
    private String pathToIdleRight = "knight/idleRight";
    private String pathToRunLeft = "knight/runLeft";
    private String pathToRunRight = "knight/runRight";

    private Skill skill5 =
            new Skill(new ArrowSkill(SkillTools::getCursorPositionAsPoint), arrowCoolDown, 3);

    private Skill skill6 =
            new Skill(
                    new BoomerangSkill(SkillTools::getCursorPositionAsPoint), boomerangCoolDown, 3);

    private Skill firstSkill, secondSkill, thirdSkill, fourthSkill;
    //private InventoryComponent inventory;
    int cd = 30;
    private HealthComponent hp;
    private IOnDeathFunction death;
    private PositionComponent position;
    private XPComponent xp;
    public PlayableComponent pc;
    private ManaComponent mc;
    private boolean gameOver = false;
    private Logger heroLogger = Logger.getLogger(Hero.class.getName());
    private Animation hitAnimation = AnimationBuilder.buildAnimation("knight/hit");
    private Animation dieAnimation = AnimationBuilder.buildAnimation("knight/death");
    private AnimationComponent heroAnimation;
    private Set<Monster> fightHandler = new HashSet<>();
    private int hitSpeed;
    private int hitPause = 0;
    private int frameTime;
    private int melee;
    private boolean isOpen = false;
    private int delay = 0;
    private HeroGraphicInventory graphicInventory;
    private HitboxComponent hitBox;
    private ItemData heroWeapon;
    private HUD hud;
    private static final Logger log = Logger.getLogger(Hero.class.getName());

    /**
     * <b><span style="color: rgba(3,71,134,1);">Logger für den Helden</span></b><br>
     * Loggen der Helden Ereignisse in der Datei Hero.txt im Ordner Logs.<br>
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_3
     * @since 21.05.2023
     */
    public static void HeroLogs() {
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("logs/log_Hero.txt", true);
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
        xp.setCurrentLevel(1);
        this.position = new PositionComponent(this);
        mc = new ManaComponent(this, 15, Constants.FRAME_RATE);
        onDeath();
        this.hp = new HealthComponent(this, 50, this.death, this.hitAnimation, this.dieAnimation);
        this.melee = 5;
        this.hitSpeed = 1;
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        pc = new PlayableComponent(this);
        setupFireballSkill();
        setupLightningLine();
        setupTransform();
        setupMindControlSkill();
        pc.setSkillSlot1(firstSkill);
        pc.setSkillSlot2(secondSkill);
        pc.setSkillSlot3(thirdSkill);
        pc.setSkillSlot4(fourthSkill);
        pc.setSkillSlot5(skill5);
        pc.setSkillSlot6(skill6);
        setDefaultItems();
        setHUD();
    }

    public PlayableComponent getSkills() {
        return pc;
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">HUD Initialisieren</span></b><br>
     * Diese Methode zeigt das Helden HUD an
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public void setHUD() {
        if(Game.controller.contains(this.hud)) {
            Game.controller.remove(this);
        }
        this.hud = new HUD(this);
    }

    public void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    public void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        this.heroAnimation = new AnimationComponent(this, idleLeft, idleRight);
    }

    public void setWeapon(ItemData weapon) {
        this.heroWeapon = weapon;
    }

    public ItemData getWeapon() {
        return this.heroWeapon;
    }

    public HealthComponent getHP() {
        return this.hp;
    }

    /** FireballSkill */
    private void setupFireballSkill() {
        fireballSkill = new FireballSkill(SkillTools::getCursorPositionAsPoint);
        firstSkill = new Skill(fireballSkill, fireballCoolDown, 1);
    }

    /**
     * Blitzschlag initialisieren. Wo der Maus point sich gerade befindet, wird der Blitz/Skill
     * aktiviert. Der CoolDown ist von der LightningLineSkill Klasse vor definiertes cool down und
     * der Skill hat ebenso sein eigene Timer für die Wiederaktivierung.
     */
    private void setupLightningLine() {
        lightningLineSkill = new LightningLineSkill(SkillTools::getCursorPositionAsPoint);
        lightningCoolDown = lightningLineSkill.getBreakTime();
        secondSkill =
                new Skill(lightningLineSkill, lightningCoolDown, lightningLineSkill.getMana());
    }

    /** TransformSkill */
    private void setupTransform() {
        transformSkill = new TransformSkill(this, 5);
        thirdSkill = new Skill(transformSkill, 0, 5);
    }

    private void setupMindControlSkill() {
        mindControlSkill = new MindControlSkill();
        fourthSkill = new Skill(mindControlSkill, 0, mindControlSkill.getMana());
    }

    /** CollisionBox */
    private void setupHitboxComponent() {
        this.hitBox = new HitboxComponent(
                this,
                (you, other, direction) -> {
                    if (you != other) {
                        for (Boss boss : Game.bosses) {
                            if (other == boss && boss != null) {
                                mindControlSkill.setOther(other);
                                mindControlSkill.setFight(true);
                            }
                        }
                    }
                    if (other instanceof Monster) {
                        fightHandler.add((Monster) other);
                    }
                },
                (you, other, direction) -> {
                    if (other instanceof Monster) {
                        fightHandler.remove(other);
                    }
                });
    }

    /** Lade standard Items */
    private void setDefaultItems() {
        this.inventory = new InventoryComponent(this, 10);
        ItemDataGenerator itm = new ItemDataGenerator();
        this.inventory.addItem(itm.getItem(0));
        this.inventory.addItem(itm.getItem(1));
    }

    /**
     * Welche Skill soll aktiviert werden.
     *
     * @param skill Erwartet einen Skill
     */
    public void execute(Skill skill) {
        if (thirdSkill == skill) {
            transformSkill.execute(this);
        }
        if (fourthSkill == skill) {
            mindControlSkill.execute(this);
        }
        if (thirdSkill != skill && fourthSkill != skill) skill.execute(this);
    }

    // Zeit für Blitzschlag
    private static long timerForLightningStart = System.currentTimeMillis();

    public void setIsOpen(boolean isOpen) {
        this.delay = 30;
        this.isOpen = isOpen;
    }

    public boolean getIsOpen() {
        return this.isOpen;
    }

    public void setGraphicInventory(HeroGraphicInventory graphicInventory) {
        this.graphicInventory = graphicInventory;
    }

    public HeroGraphicInventory getGraphicInventory() {
        return this.graphicInventory;
    }

    /**
     * Zeit für Blitzschlag. ist die Zeit für Blitz breakTime erreicht, setzt sich der Skill zurück
     * und kann wieder abgefeuert werden. Der breakTime ist random!
     */
    @Override
    public void update() {
        if(!Game.getPause()) {
            fightMonster();
            skill1_4();
        }
        if(this.delay == 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I) && this.isOpen) {
                if(Game.getPause()) {
                    Game.togglePause();
                }
                this.delay = 30;
                log.info("Inventar wird geschlossen");
                this.isOpen = false;
                this.graphicInventory.closeInventory();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.I) && !this.isOpen) {
                if(!Game.getPause()) {
                    Game.togglePause();
                }
                this.delay = 30;
                log.info("Inventar wird geöffnet");
                this.isOpen = true;
                this.graphicInventory = new HeroGraphicInventory(this);
                this.graphicInventory.openInventory();
            }
        } else {
            this.delay--;
        }
    }

    private void skill1_4() {
        long timerForLightningEnd = System.currentTimeMillis();
        if (secondSkill.isOnCoolDown()) {
            long timer = (timerForLightningEnd - timerForLightningStart) / (60 * 60);
            if (timer == lightningLineSkill.getBreakTime()) {
                lightningCoolDown = 0;
                setupLightningLine();
                timerForLightningStart = System.currentTimeMillis();
                heroLogger.info("Skill ist wieder aktiv!");
            }
        }
        // Rufe transformSkill update auf
        // Verstärke Player dmg und erhöhe die Player
        // Geschwindigkeit
        transformSkill.update();
        if (transformSkill.isMonster()) {
            int dM = Math.round(1.5f * dmg);
            dmg = Math.round(dmg + dM);
            xSpeed = 0.35f;
            ySpeed = 0.35f;
        } else {
            dmg = Math.round(0.25 * getLevel());
            xSpeed = 0.3f;
            ySpeed = 0.3f;
        }
        if (transformSkill.isBoss()) {
            int dB = Math.round(2.5f * dmg);
            dmg = Math.round(dmg + dB);
            xSpeed = 0.4f;
            ySpeed = 0.4f;
        } else {
            dmg = Math.round(0.25f * getLevel());
            xSpeed = 0.3f;
            ySpeed = 0.3f;
        }
    }

    private void fightMonster() {
        if (this.getHp().getCurrentHealthpoints() <= 0) {
            this.fightHandler.clear();
        } else {
            if (!this.fightHandler.isEmpty()) {
                this.frameTime = this.hitSpeed * Constants.FRAME_RATE;
                if (this.hitPause >= this.frameTime) {
                    this.hitPause = 0;
                    for (Monster m : this.fightHandler) {
                        if (Game.getEntities().contains(m)) {
                            m.getHp().receiveHit(new Damage(this.melee, PHYSICAL, this));
                            log.info("Hero hits " + m.getClass().getSimpleName() + ": " + this.melee + " Hp");
                        }
                    }
                } else {
                    this.hitPause++;
                }
            }
        }
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Wenn der Held stirbt</span></b><br>
     * - Leere das Kampfsystem
     * - Setze sterbe Animation
     * - Entferne den Helden
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_2
     * @since 04.06.2023
     */
    public void onDeath() {
        this.death = entity -> {
            this.hitBox = null;
            this.fightHandler = new HashSet<>();
            Game.addEntity(new DeadAnimation(this));
            log.info("Hero died.");
            Game.removeEntity(this);
            gameOver = true;
            Game.gameOver();
        };
    }

    public Set<Monster> getFightHandler() {
        return this.fightHandler;
    }

    /** Setze die Hero Attributen auf 0 */
    public void clear() {
        pc = null;
        mc = null;
        hp = null;
        melee = 0;
        dmg = 0;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Position</span></b><br>
     * Position des Helden im Dungeon Level.
     *
     * @return PositionComponent gibt die Position des Helden zurück
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_2
     * @since 08.05.2023
     */
    public PositionComponent getPosition() {
        return position;
    }

    public void setPosition(PositionComponent position) {
        this.position = position;
    }

    public void setMaxHp(int level) {
        this.hp.setMaximalHealthpoints(this.hp.getMaximalHealthpoints() * (level * 1));
    }

    public void setMeleeDmg(int level) {
        this.melee = this.melee * (level * 1);
    }

    public int getDmg() {
        return this.melee;
    }

    public XPComponent getXP() {
        return this.xp;
    }

    /**
     * @param lootXP wie viel lootXP der Spieler bekommt
     */
    public void setLootXP(long lootXP) {
        xp.addXP(lootXP);
        heroLogger.info(
                "CurrentXP (Hero): "
                        + xp.getCurrentXP()
                        + "\nMaximale Xp (Hero): "
                        + xp.getMaxXP());
        if (xp.getCurrentXP() >= xp.getMaxXP()) {
            xp.setCurrentLevel(xp.getCurrentLevel() + 1);
            xp.setCurrentXP(xp.getCurrentXP() - xp.getMaxXP());
            xp.getMaxXPToNextLevel();
            lvlUP();
            if (xp.getCurrentXP() >= xp.getMaxXP()) {
                setLootXP(xp.getCurrentXP() - xp.getMaxXP());
            }
        }
    }

    /** Level up */
    public void lvlUP() {
        System.out.println("Level UP!! (" + xp.getCurrentLevel() + ")");
        mc.generateManaPointToNextLevel();
    }

    /**
     * Die Hero HP werden nach der Kampf gesetzt. Außerdem wird es überprüft, ob die hp <= 0 sind
     * oder nicht.
     *
     * @param heroDMG HealthComponent object
     */
    public void setHp(int heroDMG) {
        int h = this.hp.getMaximalHealthpoints() - heroDMG;
        if (h <= 0) {
            this.hp.setMaximalHealthpoints(0);
            this.hp.setCurrentHealthpoints(0);
        } else {
            int reg = (hp.getMaximalHealthpoints() * 20) / 100;
            heroLogger.info("Hero hat nach dem Kampf: " + this.hp.getCurrentHealthpoints() + "hp");
            this.hp.setCurrentHealthpoints(this.hp.getCurrentHealthpoints() + reg);
            heroLogger.info("Es wurden " + reg + "hp regeneriert!");
        }
    }

    public HealthComponent getHp() {
        return hp;
    }

    public ManaComponent getMc() {
        return mc;
    }

    /**
     * Prüft und rechnet wie viel der Hero an Damages gemacht hat.
     *
     * @return wie viel an Damage hat der Hero gemacht.
     */
    public long damage() {
        dmg += Math.round(0.25 * getLevel());
        int dmgFire = fireballSkill.getDamage().damageAmount();
        int dmgLightning = lightningLineSkill.getDamage();
        if (!firstSkill.isOnCoolDown()) {
            dmg += dmgFire;
        }
        if (!secondSkill.isOnCoolDown()) {
            dmg += dmgLightning;
        }
        return dmg;
    }

    public void isOnAttack(boolean attack) {
        if (attack) {
            damage();
        }
    }
    /**
     * @return Aktuelle Hero Level wird zurückgeworfen.
     */
    public long getLevel() {
        assert xp != null;
        return xp.getCurrentLevel();
    }

    /**
     * @return nur für LightningLine, wird überprüft ob der Level zu niedrig ist oder der Skill
     *     wurde schon ausgeführt.
     */
    public boolean requiredLevel() {
        return lightningLineSkill.isRequiredLevel();
    }

    /** Konsolenausgabe (Taste: I) */
    public void info() {
        heroLogger.info(
                "HP: " + hp.getCurrentHealthpoints() + " von " + hp.getMaximalHealthpoints());
        if (xp.getCurrentXP() <= 0) xp.setCurrentXP(0);
        heroLogger.info("XP: " + xp.getCurrentXP() + " von " + xp.getMaxXP());
        if (mc.getCurrentManaPoint() <= 0) mc.setCurrentManaPoint(0);
        heroLogger.info("Mana: " + mc.getCurrentManaPoint() + " von " + mc.getMaxManaPoint());
        heroLogger.info("Melee: " + melee);
        heroLogger.info("CurrentLevel: " + xp.getCurrentLevel());
    }

    public String getPathToIdleLeft() {
        return pathToIdleLeft;
    }

    public String getPathToIdleRight() {
        return pathToIdleRight;
    }

    public String getPathToRunLeft() {
        return pathToRunLeft;
    }

    public String getPathToRunRight() {
        return pathToRunRight;
    }

    public void setPathToIdleLeft(String left) {
        this.pathToIdleLeft = left;
    }

    public void setPathToIdleRight(String right) {
        this.pathToIdleRight = right;
    }

    public void setPathToRunLeft(String runLeft) {
        this.pathToRunLeft = runLeft;
    }

    public void setPathToRunRight(String runRight) {
        this.pathToRunRight = runRight;
    }
}
