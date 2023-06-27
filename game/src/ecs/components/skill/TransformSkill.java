package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import ecs.entities.Hero;
import java.util.Random;
import java.util.logging.Logger;

/**
 * TransformSkill
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 22.05.2023
 */
public class TransformSkill implements ISkillFunction {
    private long mana; // Mana Point zum Abziehen
    private int lebenspunkte = 5; // Zusätzlich Lebenspunkte Abziehen
    private Entity entity; // Hero entity
    private static final String TRANSFORMPATHTYPE1 = "monster/type1/";
    private static final String TRANSFORMPATHTYPE2 = "monster/type2/";
    private static final String TRANSFORMPATHTYPE5 = "monster/type5/";
    private String pathToIdleLeft; // transform path left
    private String pathToIdleRight; // transform path right
    private String pathToRunLeft; // transform path run left
    private String pathToRunRight; // transform path run right
    private boolean isActive = true, requiredLevel = false, monster = false, boss = false;
    private final String[] defaultTexturHero = new String[4];
    private static final Logger transformLogger = Logger.getLogger(TransformSkill.class.getName());

    /**
     * Konstruktor initialisiert die Variablen und ruft die Methode setupAnimation auf
     *
     * @param mana Mana Point
     * @param entity Hero entity
     */
    public TransformSkill(Entity entity, int mana) {
        this.mana = mana;
        this.entity = entity;
        setupAnimation();
        saveHeroTextur();
    }

    /** Hier wird überprüft, welche Level der Spieler hat und die passende Verwandlung angezeigt. */
    private void setupAnimation() {
        Hero hero = (Hero) entity;
        // Monster Textur wird zwischen Level 10 und 20 geladen
        if (hero.getLevel() >= 10 && hero.getLevel() <= 20) {
            loadMonster(""); // Empty ladet normale Monster
            monster = true; // Monster verwandlung ist aktiv
            requiredLevel = true; // Erreichte level ist true
        }
        // Level 20 und höher erreicht, lade nur noch Boss textur
        if (hero.getLevel() >= 20) {
            loadMonster("boss/"); // Boss verzeichnis
            mana = Math.round(0.5f * hero.getLevel()); // Mana Point
            lebenspunkte = Math.round(0.25f * hero.getLevel()); // Schadenpunkte
            monster = !monster; // Monster auf false setzen
            boss = true; // Boss ist aktive
            requiredLevel = true; // Erreichte level ist true
        }
    }

    /**
     * @param name welche Textur soll geladen werden.
     */
    private void loadMonster(String name) {
        switch (new Random().nextInt(3)) {
            case 0 -> {
                pathToIdleLeft = TRANSFORMPATHTYPE1 + name + "idleLeft";
                pathToIdleRight = TRANSFORMPATHTYPE1 + name + "idleRight";
                pathToRunLeft = TRANSFORMPATHTYPE1 + name + "runLeft";
                pathToRunRight = TRANSFORMPATHTYPE1 + name + "runRight";
            }
            case 1 -> {
                pathToIdleLeft = TRANSFORMPATHTYPE2 + name + "idleLeft";
                pathToIdleRight = TRANSFORMPATHTYPE2 + name + "idleRight";
                pathToRunLeft = TRANSFORMPATHTYPE2 + name + "runLeft";
                pathToRunRight = TRANSFORMPATHTYPE2 + name + "runRight";
            }
            case 2 -> {
                pathToIdleLeft = TRANSFORMPATHTYPE5 + name + "idleLeft";
                pathToIdleRight = TRANSFORMPATHTYPE5 + name + "idleRight";
                pathToRunLeft = TRANSFORMPATHTYPE5 + name + "runLeft";
                pathToRunRight = TRANSFORMPATHTYPE5 + name + "runRight";
            }
        }
    }

    /** Speichere die Hero Texturen bevor die verwandlung beginnt. */
    private void saveHeroTextur() {
        Hero hero = (Hero) entity;
        defaultTexturHero[0] = hero.getPathToIdleLeft();
        defaultTexturHero[1] = hero.getPathToIdleRight();
        defaultTexturHero[2] = hero.getPathToRunLeft();
        defaultTexturHero[3] = hero.getPathToRunRight();
    }

    // Timer start
    private long timerForTexturStart = System.currentTimeMillis();
    // random Zeit zwischen 5s und 10s
    // bis sich die Transformation auflöst und der Spieler seine ursprüngliche
    // Textur erhält.
    private final int min = 5; // Zwischen 5s und 10s
    private final int max = 10; // Zwischen 5s und 10s
    private int randomTimer = new Random().nextInt(max - min + 1) + min; // randomTime

    /**
     * Prüft bei jedes Frame, ob die Zeit abgelaufen ist und die Original Textur laden soll oder
     * nicht.
     */
    private long timer;

    public void update() {
        if (requiredLevel) {
            long timerForTexturEnd = System.currentTimeMillis();
            timer = (timerForTexturEnd - timerForTexturStart) / (60 * 60);
            if (!isActive && timer == randomTimer) {
                loadDefaultTextur();
                timerForTexturStart = System.currentTimeMillis();
                isActive = !isActive;
                randomTimer = new Random().nextInt(max - min + 1) + min; // randomTime
            }
        }
    }

    /**
     * Ob der Skill aktiv ist.
     *
     * @return Wahr wird zurückgeworfen, wenn der Skill aktiv ist. Ansonsten falsch.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Um den Skill nochmal zu benutzen können.
     *
     * @return Zeit in Sekunden wird ausgegeben.
     */
    public int getTimer() {
        return (int) timer;
    }

    /**
     * CoolDown bzw. BreakTime
     *
     * @return breakTime der Skill wird zurückgeworfen.
     */
    public int getBreakTime() {
        return randomTimer;
    }

    /**
     * Aktion ausführen
     *
     * @param entity which uses the skill
     */
    @Override
    public void execute(Entity entity) {
        Hero hero = (Hero) entity;
        if (requiredLevel) { // Erforderliche Level erreicht
            if (isActive && isAnimationLoaded()) { // Beim ersten verwandlung isAction true
                loadMonsterTextur(); // lade monster texturen
                isActive = !isActive; // setze isAktive auf true, false
                /*
                   Sind Monster texturen aktive
                   nur Mana Point(wenn vorhanden), werden
                   abgezogen. Ansonsten Player HP
                */
                if (monster) {
                    if (hero.getMc().getCurrentManaPoint() >= 0) {
                        hero.getMc()
                                .setCurrentManaPoint(
                                        hero.getMc().getCurrentManaPoint() - (int) mana);
                        transformLogger.info(
                                "Transform kostet ("
                                        + mana
                                        + ") und Hero hat "
                                        + hero.getMc().getCurrentManaPoint()
                                        + " mana.");
                    }
                    if (mana <= 0 && hero.getHp().getCurrentHealthpoints() >= 0) {
                        hero.getHp()
                                .receiveHit(
                                        new Damage(
                                                hero.getHp().getCurrentHealthpoints() - (int) mana,
                                                DamageType.PHYSICAL,
                                                null));
                        transformLogger.info(
                                "Mana sind alle, ziehe von Lebenspunkte ab ("
                                        + mana
                                        + " mana): "
                                        + "Hero hat "
                                        + hero.getHp().getCurrentHealthpoints()
                                        + "hp");
                    }
                }
                /*
                   Boss Texturen sind aktive bzw. Boss level erreicht.
                   Weil Mana und Lebenspunkte abgezogen werden,
                   sind hier zwei abfragen. Abfrage eins der Mana Point
                   ist 0, dann soll von Player HP abgezogen werden.
                   Ansonsten ziehe von mana und zusätzlich von Lebenspunkte ab.
                */
                if (boss) {
                    if (hero.getHp().getCurrentHealthpoints() >= 0) {
                        if (hero.getMc().getCurrentManaPoint() - mana > 0) {
                            int heroMana = hero.getMc().getCurrentManaPoint() - (int) mana;

                            if (heroMana <= 0) {
                                hero.getHp()
                                        .receiveHit(
                                                new Damage(
                                                        hero.getHp().getCurrentHealthpoints()
                                                                + heroMana,
                                                        DamageType.PHYSICAL,
                                                        null));
                                System.out.println(
                                        "Hero Mana ist unter 0 und "
                                                + "falls die Mana im negativen Bereich sind, Ziehe von Hero hp"
                                                + " ab!");
                                System.out.println(
                                        "Es werden "
                                                + Math.abs(heroMana)
                                                + " mana von Hero hp abgezogen.");
                            } else {
                                hero.getMc()
                                        .setCurrentManaPoint(
                                                hero.getMc().getCurrentManaPoint() - (int) mana);
                            }
                        } else {
                            hero.getMc()
                                    .setCurrentManaPoint(
                                            hero.getMc().getCurrentManaPoint() - (int) mana);
                            System.out.println(
                                    "Hero mana sind jetzt: "
                                            + hero.getMc().getCurrentManaPoint()
                                            + ", davon sind "
                                            + mana
                                            + " abgezogen!!");
                            hero.getHp()
                                    .receiveHit(
                                            new Damage(
                                                    hero.getHp().getCurrentHealthpoints()
                                                            - (int) mana,
                                                    DamageType.PHYSICAL,
                                                    null));
                            System.out.println(
                                    "Zusaetzlich werden "
                                            + mana
                                            + "hp"
                                            + " von HeroHP abgezogen! Hero hat noch "
                                            + hero.getHp().getCurrentHealthpoints()
                                            + "hp");
                        }
                    }
                }
            }
        } else { // Erforderliche Level nicht erreicht!
            transformLogger.info(
                    "Dein Level ist zu niedrig!!! (" + getClass().getSimpleName() + ")");
        }
    }

    /** Load Monster textur */
    private void loadMonsterTextur() {
        Hero hero = (Hero) entity;
        hero.setPathToIdleLeft(pathToIdleLeft);
        hero.setPathToIdleRight(pathToIdleRight);
        hero.setPathToRunLeft(pathToRunLeft);
        hero.setPathToRunRight(pathToRunRight);
        hero.setupVelocityComponent();
        hero.setupAnimationComponent();
    }

    /** Load default textur of hero */
    private void loadDefaultTextur() {
        Hero hero = (Hero) entity;
        hero.setPathToIdleLeft(defaultTexturHero[0]);
        hero.setPathToIdleRight(defaultTexturHero[1]);
        hero.setPathToRunLeft(defaultTexturHero[2]);
        hero.setPathToRunRight(defaultTexturHero[3]);
        hero.setupVelocityComponent();
        hero.setupAnimationComponent();
    }

    /**
     * Test
     *
     * @param requiredLevel Erforderliche Level erreicht
     */
    public void setRequiredLevel(boolean requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public boolean isRequiredLevel() {
        return requiredLevel;
    }

    /**
     * @return wahr die Texturen sind geladen ansonsten false
     */
    private boolean isAnimationLoaded() {
        return pathToIdleLeft != null
                && pathToIdleRight != null
                && pathToRunLeft != null
                && pathToRunRight != null;
    }

    /**
     * @return Sind Monster Texturen aktiv
     */
    public boolean isMonster() {
        return monster;
    }

    /**
     * @return sind Boss Texturen aktiv
     */
    public boolean isBoss() {
        return boss;
    }
}
