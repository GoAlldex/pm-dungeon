package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.components.xp.XPComponent;
import ecs.entities.boss.Boss;
import ecs.items.ItemDataGenerator;
import graphic.Animation;
import java.util.logging.Logger;
import starter.Game;
import tools.Constants;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity {

    private int fireballCoolDown = 0, lightningCoolDown = 0;
    private LightningLineSkill lightningLineSkill;
    private FireballSkill fireballSkill;
    private TransformSkill transformSkill;
    private MindControlSkill mindControlSkill;
    private float xSpeed = 0.3f;
    private float ySpeed = 0.3f;

    private String pathToIdleLeft = "knight/idleLeft";
    private String pathToIdleRight = "knight/idleRight";
    private String pathToRunLeft = "knight/runLeft";
    private String pathToRunRight = "knight/runRight";
    private Skill firstSkill, secondSkill, thirdSkill, fourthSkill;
    private InventoryComponent inventory;
    int cd = 30;
    private HealthComponent hp;
    private IOnDeathFunction death;
    private PositionComponent position;
    private XPComponent xp;
    public PlayableComponent pc;
    private ManaComponent mc;
    private boolean gameOver = false;
    private Logger heroLogger = Logger.getLogger(Hero.class.getName());

    /** Entity with Components */
    public Hero() {
        super();
        this.position = new PositionComponent(this);
        mc = new ManaComponent(this, 15, Constants.FRAME_RATE);
        xp = new XPComponent(this);
        xp.setCurrentLevel(1);
        this.hp = new HealthComponent(this);
        this.hp.setMaximalHealthpoints(100);
        this.hp.setCurrentHealthpoints(hp.getMaximalHealthpoints());
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
        setDefaultItems();
    }

    public void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    public void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
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
        heroLogger.info("CoolDown for lightning: " + lightningCoolDown);
        secondSkill =
                new Skill(lightningLineSkill, lightningCoolDown, lightningLineSkill.getMana());
    }

    /** TransformSkill */
    private void setupTransform() {
        transformSkill = new TransformSkill(this, 5);
        thirdSkill = new Skill(transformSkill, 0, 5);
    }

    private void setupMindControlSkill() {
        mindControlSkill = new MindControlSkill((int) getLevel());
        fourthSkill = new Skill(mindControlSkill, 0, mindControlSkill.getMana());
    }

    /** CollisionBox */
    private void setupHitboxComponent() {
        new HitboxComponent(
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
                    System.out.println("heroCollisionEnter");
                },
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    /** Lade standard Items */
    private void setDefaultItems() {
        this.inventory = new InventoryComponent(this, 9);
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

    /**
     * Zeit für Blitzschlag. ist die Zeit für Blitz breakTime erreicht, setzt sich der Skill zurück
     * und kann wieder abgefeuert werden. Der breakTime ist random!
     */
    @Override
    public void update() {
        long timerForLightningEnd = System.currentTimeMillis();
        if (secondSkill.isOnCoolDown()) {
            long timer = (timerForLightningEnd - timerForLightningStart) / (60 * 60);
            if (timer == lightningLineSkill.getBreakTime()) {
                lightningCoolDown = 0;
                setupLightningLine();
                timerForLightningStart = System.currentTimeMillis();
                heroLogger.info("Skill ist wieder aktiv!");
            }
            // heroLogger.info("Mana von Hero: " + mc.getCurrentManaPoint());
            // heroLogger.info("CoolDown: "+lightningCoolDown+" | Timer: " + (timerForLightningEnd -
            // timerForLightningStart) / (60*60) + "s");
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
        // Prüfe ob GameOver ist
        gameOver();
    }

    @Override
    public InventoryComponent getInventory() {
        return this.inventory;
    }

    public void onDeath() {}

    public void gameOver() {
        if (hp != null) {
            if (hp.getCurrentHealthpoints() <= 0) {
                pc = null;
                gameOver = true;
                mc = null;
                hp = null;
                dmg = 0;
                Game.removeEntity(this);
                Game.gameOver();
            }
        }
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
     * oder nicht falls ja, wird die methode gameOver aufgerufen.
     *
     * @param heroDMG HealthComponent object
     */
    public void setHp(int heroDMG) {
        int h = this.hp.getMaximalHealthpoints() - heroDMG;
        if (h <= 0) {
            System.out.println("GameOver");
            this.hp.setCurrentHealthpoints(0);
            gameOver();
        } else {
            int reg = (hp.getMaximalHealthpoints() * 20) / 100;
            System.out.println(
                    "Hero hat nach dem Kampf: " + this.hp.getCurrentHealthpoints() + "hp");
            this.hp.setCurrentHealthpoints(this.hp.getCurrentHealthpoints() + reg);
            System.out.println("Es wurden " + reg + "hp regeneriert!");
        }
    }

    public HealthComponent getHp() {
        return hp;
    }

    public ManaComponent getMc() {
        return mc;
    }

    private long dmg;

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
        System.out.println(
                "HP: " + hp.getCurrentHealthpoints() + " von " + hp.getMaximalHealthpoints());
        if (xp.getCurrentXP() <= 0) {
            xp.setCurrentXP(0);
        }
        System.out.println("XP: " + xp.getCurrentXP() + " von " + xp.getMaxXP());
        System.out.println("CurrentLevel: " + xp.getCurrentLevel());
        if (mc.getCurrentManaPoint() <= 0) mc.setCurrentManaPoint(0);
        System.out.println("Mana: " + mc.getCurrentManaPoint() + " von " + mc.getMaxManaPoint());
    }

    /** Set pathTo animation */
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
}
