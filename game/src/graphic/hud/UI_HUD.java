package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import controller.ScreenController;
import ecs.entities.Hero;
import java.util.*;
import java.util.logging.Logger;
import starter.Game;
import tools.Constants;
import tools.Point;

/**
 * Die Klasse zeigt auf dem UI folgende Elemente & Skills. <br>
 * - Skills <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;o Fire <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;o Lightning <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;o MindControl <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;o Transform <br>
 * - Cooldown <br>
 * - Hero Level <br>
 * - Hero HP <br>
 * - Hero XP <br>
 * - Hero Mana <br>
 * - Dungeon Level <br>
 * - Sowie die Tastenbefehle für Skills. <br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version V1
 * @since 18.06.2023
 */
public class UI_HUD<T extends Actor> extends ScreenController<T> {

    // Attributen
    private static final String PATH_TO_BACKGROUND_DISABLED = "hud/ui/gray.png"; // Disable
    private static final String PATH_TO_SKILL_BACKGROUND =
            "hud/ui/background_Skills.png"; // Hintergrundbild
    private static final String[] PATH_TO_TEXT_BACKGROUND = {
        "hud/ui/backgroundText32_16.png", "hud/ui/backgroundText128_64.png"
    }; // Hintergrundbild - Größen
    private final Logger UI_LOGGER = Logger.getLogger(UI_HUD.class.getSimpleName()); // Logger
    /*
       Benutzung das Array PATH_TO_SKILLS[]
       PATH_TO_SKILLS[0..3] = Skills die F Tasten benutzen von F1 - F4
       PATH_TO_SKILLS[4..5] = Weitere Skills die Buchstaben benutzen.
    */
    private static final String[] PATH_TO_SKILLS = {
        "skills/fireball/fireBall_Down/fireBall_Down1.png",
        "skills/lightningSkill/lightning_line0.png",
        "hud/ui/Transform.png",
        "hud/ui/mindControlIcon.png",
        "skills/boomerang/Boomerang1.png",
        "skills/arrow/arrow1.png"
    };

    private static Map<Setup_Skill, Skill> skillSlots = new HashMap<>();
    private static List<ScreenImage> images = new ArrayList<>();
    private static List<ScreenImage> imagesDisable = new ArrayList<>();
    private Hero hero;
    private FontBuilder fontBuilder = new FontBuilder("hud/font/Algerian.ttf");
    private FontBuilder timerFontBuilder = new FontBuilder("hud/font/Caprasimo.ttf");
    private ScreenText txtTransformTimer, txtLightningTimer, txtBoomerangTimer, txtArrowTimer;
    private ScreenText levelText;
    private ScreenText hpText;
    private ScreenText txtMana;
    private ScreenText txtXP;
    private ScreenText txtDungeonLevel;
    private int sizeOfSkills = 6;
    private boolean setup = true;
    private boolean erforderlicheLevel = false;

    /**
     * Leerer Konstruktor. <br>
     * Wenn es aufgerufen wird, ruft das andere Konstruktor und übergebt einen Neuen SpriteBatch
     * objekt.
     */
    public UI_HUD() {
        this(new SpriteBatch());
    }

    /**
     * Konstruktor mit einem Parameter von SpriteBatch objekt. <br>
     * Hier werden die methoden für setupSkills() & setupHeroSetting() aufgerufen.
     *
     * @param batch erwartet einen batch Objekt
     */
    public UI_HUD(SpriteBatch batch) {
        super(batch);
        loadDefaultSkill();
        fontBuilder.setSize(13);
        fontBuilder.setFontColor(Color.BLACK);
        timerFontBuilder.setSize(14);
        timerFontBuilder.setFontColor(Color.RED);
        setupHeroSetting();
        show();
    }

    /** Wird für die Spieler sichtbar */
    public void show() {
        this.forEach(
                (Actor s) -> {
                    s.setVisible(true);
                });
    }

    /** Die Spieler können UI nicht sehen, wenn dieses Methode aufgerufen wird. */
    public void hidden() {
        this.forEach((Actor s) -> s.setVisible(false));
    }

    /**
     * SwitchSkill ändert die Skill slots. Mit der Methode switchSkill, kann man die Skills ändern.
     * <br>
     * Beispiel: Skill am Slot 2 mit dem Slot 1 Skill tauschen. <code>
     * switch(Setup_Skill.SECOND_SKILL, Setup_Skill.FIRST_SKILL)</code>
     *
     * @param skill1 Skill auswahl, zwischen skill 1 und 6.
     * @param skill2 Tauschplatz bzw. Tauschskill auswählen.
     */
    public void switchSkill(Setup_Skill skill1, Setup_Skill skill2) {
        removeSkillSlotImage(skill1, skill2);
        switchSkillSlot(skill1, skill2);
    }

    /*
       In dieser Methode werden die Heros Einstellung auf dem UI angezeigt.
       Das wären:
           - Cooldown
           - Hero Level
           - Hero HP
           - Hero XP
           - Hero Mana
           - Dungeon Level
    */
    private void setupHeroSetting() {
        if (Game.getHero().isPresent()) {
            addLevel();
            addHP();
            addMana();
            addXP();
            addDungeonLevel();
        }
    }

    /**
     * Die Methode aktualisiert alle Parametern, die auf dem UI zu sehen sind.<br>
     * Das sind:<br>
     * Hero Level<br>
     * Hero HP<br>
     * Hero XP<br>
     * Hero Mana<br>
     * Dungeon Level<br>
     * <br>
     * sowie die Timer, wenn ein Skill aktiviert wird (falls CoolDown nicht auf 0 ist).
     */
    public void updateUI() {
        // Am anfang werden einmalig alle Einstellungen und Skills initialisiert.
        if (setup) {
            setupHeroSetting();
            loadDefaultSkill();
            setup = false;
        }

        // Die Texte werden bei jedem frame aktualisiert.
        levelText.setText("Level " + hero.getLevel());
        hpText.setText(
                "HP: "
                        + hero.getHp().getCurrentHealthpoints()
                        + "/"
                        + hero.getHp().getMaximalHealthpoints());
        txtMana.setText(
                "Mana "
                        + hero.getMc().getCurrentManaPoint()
                        + "/"
                        + hero.getMc().getMaxManaPoint());
        txtDungeonLevel.setText("Dungeon Level: " + Game.levelCounter);
        txtXP.setText("XP: " + hero.getXP().getCurrentXP() + "/" + hero.getXP().getMaxXP());

        // prüft am Anfang, welche Skill noch nicht freigeschaltet sind.
        if (hero != null && !erforderlicheLevel) {
            if (!hero.requiredLevel()) {
                addDisableBackground(Skill.LIGHTNING.name(), secondSkillX());
            }
            if (!hero.getTransform().isRequiredLevel()) {
                addDisableBackground(Skill.TRANSFORM.name(), thirdSkillX());
            }
            if (!hero.getMindControlSkill().isRequiredLevel()) {
                addDisableBackground(Skill.MIND_CONTROL.name(), fourtSkillX());
            }
            erforderlicheLevel = true;
        }
        // Erforderliche Level für skills erreicht, wird der Skill freigeschaltet.
        // Außerdem wenn der skill ich in cooldown befindet, aktiviert sich der Timer.
        if (hero != null) {
            if (hero.getLevel() >= 3) {
                removeDisabledImages(Skill.LIGHTNING);
                if (hero.getLightningCoolDown()) {
                    addDisableBackground(Skill.LIGHTNING.name(), secondSkillX());
                    if (txtLightningTimer != null) {
                        addLightningTimer();
                    }
                    txtLightningTimer.setText(
                            ""
                                    + hero.getTimerForLightning()
                                    + "/"
                                    + hero.getLightningLineSkill().getBreakTime());
                }
            }
            if (hero.getLevel() >= 10) {
                removeDisabledImages(Skill.TRANSFORM);
                if (!hero.getTransform().isActive()) {
                    addDisableBackground(Skill.TRANSFORM.name(), thirdSkillX());
                    if (txtTransformTimer != null) {
                        addTransformTimer();
                    }
                    txtTransformTimer.setText(
                            ""
                                    + hero.getTransform().getTimer()
                                    + "/"
                                    + hero.getTransform().getBreakTime());
                }
            }
            if (hero.getLevel() >= 15) {
                removeDisabledImages(Skill.MIND_CONTROL);
            }
            if (hero.pc.getSkillSlot5().get().isOnCoolDown()) {
                removeDisabledImages(Skill.ARROW);
                addDisableBackground(Skill.ARROW.name(), sixSkillX());
                if (txtArrowTimer != null) {
                    addArrowTimer();
                }
                txtArrowTimer.setText("" + hero.getTimerForArrow() + "/" + hero.getArrowCoolDown());
            } else {
                removeDisabledImages(Skill.ARROW);
            }
            if (hero.pc.getSkillSlot6().get().isOnCoolDown()) {
                removeDisabledImages(Skill.BOOMERANG);
                addDisableBackground(Skill.BOOMERANG.name(), fiveSkillX());
                if (txtBoomerangTimer != null) {
                    addBoomerangTimer();
                }
                txtBoomerangTimer.setText(
                        "" + hero.getTimerForBoomerang() + "/" + hero.getBoomerangCoolDown());
            } else {
                removeDisabledImages(Skill.BOOMERANG);
            }
        }
    }

    /*
       Die Methode löscht das graue Hintergrundbild.
    */
    private void removeDisabledImages(Skill skill) {
        for (ScreenImage image : imagesDisable) {
            if (image.getName().equalsIgnoreCase(Skill.LIGHTNING.name())
                    && skill.equals(Skill.LIGHTNING)) {
                remove((T) txtLightningTimer);
                remove((T) image);
            }
            if (image.getName().equalsIgnoreCase(Skill.TRANSFORM.name())
                    && skill.equals(Skill.TRANSFORM)) {
                remove((T) txtTransformTimer);
                remove((T) image);
            }
            if (image.getName().equalsIgnoreCase(Skill.MIND_CONTROL.name())
                    && skill.equals(Skill.MIND_CONTROL)) {
                remove((T) image);
            }
            if (image.getName().equalsIgnoreCase(Skill.BOOMERANG.name())
                    && skill.equals(Skill.BOOMERANG)) {
                remove((T) txtBoomerangTimer);
                remove((T) image);
            }
            if (image.getName().equalsIgnoreCase(Skill.ARROW.name()) && skill.equals(Skill.ARROW)) {
                remove((T) txtArrowTimer);
                remove((T) image);
            }
        }
    }

    /*
       Level werden oben rechts angezeigt
    */
    private void addLevel() {
        ScreenImage imgLevel = new ScreenImage(PATH_TO_TEXT_BACKGROUND[0], new Point(0, 0));
        levelText =
                new ScreenText(
                        "Level " + hero.getLevel(),
                        new Point(0, 0),
                        10,
                        ScreenText.DEFAULT_LABEL_STYLE);
        levelText.setStyle(new LabelStyleBuilder(fontBuilder.build()).build());
        Point poinLevelText =
                new Point(
                        Constants.WINDOW_WIDTH - (imgLevel.getWidth() + levelText.getWidth()) + 2,
                        Constants.WINDOW_HEIGHT - (imgLevel.getHeight() / 2f) - 22);
        levelText.setPosition(poinLevelText.x, poinLevelText.y);
        levelText.setScale(1.5f);
        Point poinLevel =
                new Point(
                        Constants.WINDOW_WIDTH - imgLevel.getWidth() - 50,
                        Constants.WINDOW_HEIGHT - imgLevel.getHeight() - 20);
        imgLevel.setPosition(poinLevel.x, poinLevel.y);
        add((T) imgLevel);
        add((T) levelText);
    }

    /*
       HP werden oben rechts angezeigt
    */
    private void addHP() {
        ScreenImage imgHP = new ScreenImage(PATH_TO_TEXT_BACKGROUND[0], new Point(0, 0));
        imgHP.setWidth(50);
        hpText =
                new ScreenText(
                        "HP: "
                                + hero.getHp().getCurrentHealthpoints()
                                + "/"
                                + hero.getHp().getMaximalHealthpoints(),
                        new Point(0, 0),
                        10,
                        ScreenText.DEFAULT_LABEL_STYLE);
        hpText.setStyle(new LabelStyleBuilder(fontBuilder.build()).build());
        Point poinLevelText =
                new Point(
                        (Constants.WINDOW_WIDTH - (imgHP.getWidth() + hpText.getWidth())) - 50,
                        Constants.WINDOW_HEIGHT - (hpText.getHeight() / 2f) - 20);
        hpText.setPosition(poinLevelText.x, poinLevelText.y);
        hpText.setScale(0.8f);
        Point poinLevel =
                new Point(
                        Constants.WINDOW_WIDTH - imgHP.getWidth() - 140,
                        Constants.WINDOW_HEIGHT - imgHP.getHeight() - 20);
        imgHP.setPosition(poinLevel.x, poinLevel.y);
        add((T) imgHP);
        add((T) hpText);
    }

    /*
       Mana werden oben links angezeigt
    */
    private void addMana() {
        ScreenImage imgMana = new ScreenImage(PATH_TO_TEXT_BACKGROUND[0], new Point(0, 0));
        imgMana.setWidth(60);
        txtMana =
                new ScreenText(
                        "Mana "
                                + hero.getMc().getCurrentManaPoint()
                                + "/"
                                + hero.getMc().getMaxManaPoint(),
                        new Point(0, 0),
                        10,
                        ScreenText.DEFAULT_LABEL_STYLE);
        txtMana.setStyle(new LabelStyleBuilder(fontBuilder.build()).build());
        Point poinLevelText =
                new Point(
                        (txtMana.getWidth() / 2f) - 10,
                        Constants.WINDOW_HEIGHT - (imgMana.getHeight() / 2f) - 22);
        txtMana.setPosition(poinLevelText.x, poinLevelText.y);
        txtMana.setScale(1.5f);
        Point poinLevel =
                new Point(
                        imgMana.getWidth() / 2f - 15,
                        Constants.WINDOW_HEIGHT - imgMana.getHeight() - 20);
        imgMana.setPosition(poinLevel.x, poinLevel.y);
        add((T) imgMana);
        add((T) txtMana);
    }

    /*
       XP werden oben links angezeigt
    */
    private void addXP() {
        ScreenImage imgXP = new ScreenImage(PATH_TO_TEXT_BACKGROUND[0], new Point(0, 0));
        imgXP.setWidth(50);
        txtXP =
                new ScreenText(
                        "XP: " + hero.getXP().getCurrentXP() + "/" + hero.getXP().getMaxXP(),
                        new Point(0, 0),
                        10,
                        ScreenText.DEFAULT_LABEL_STYLE);
        txtXP.setStyle(new LabelStyleBuilder(fontBuilder.build()).build());
        Point poinLevelText =
                new Point(
                        (txtXP.getWidth() / 2f) + (imgXP.getWidth() / 2) + 122,
                        Constants.WINDOW_HEIGHT - (imgXP.getHeight() / 2f) - 20);
        txtXP.setPosition(poinLevelText.x, poinLevelText.y);
        txtXP.setScale(1.5f);
        Point poinLevel =
                new Point(
                        140 + imgXP.getWidth() / 2f,
                        Constants.WINDOW_HEIGHT - imgXP.getHeight() - 20);
        imgXP.setPosition(poinLevel.x, poinLevel.y);
        add((T) imgXP);
        add((T) txtXP);
    }

    /*
       DungeonLevel wird unten links angezeigt.
    */
    private void addDungeonLevel() {
        ScreenImage imgDungeonLevel = new ScreenImage(PATH_TO_TEXT_BACKGROUND[0], new Point(0, 0));
        imgDungeonLevel.setWidth(70);
        txtDungeonLevel =
                new ScreenText(
                        "Dungeon Level: " + Game.levelCounter,
                        new Point(0, 0),
                        10,
                        ScreenText.DEFAULT_LABEL_STYLE);
        txtDungeonLevel.setStyle(new LabelStyleBuilder(fontBuilder.build()).build());
        Point poinLevelText = new Point(30, 15);
        txtDungeonLevel.setPosition(poinLevelText.x, poinLevelText.y);
        txtDungeonLevel.setScale(1.5f);
        Point poinLevel = new Point(20, 10);
        imgDungeonLevel.setPosition(poinLevel.x, poinLevel.y);
        add((T) imgDungeonLevel);
        add((T) txtDungeonLevel);
    }

    /*
        Dieser Methode lädt standardwerte her.
        Außerdem fügt zusätzlich folgende skills hinzu.
        FireBall
        Lightning
        Transform
        MindControl
        Boomerang
        Arrow
    */
    private void loadDefaultSkill() {
        addBackground(firstSkillX());
        addBackground(secondSkillX());
        addBackground(thirdSkillX());
        addBackground(fourtSkillX());
        addBackground(fiveSkillX());
        addBackground(sixSkillX());
        // Standardwerte
        // Fireball
        skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.FIRE);
        setupSkill(
                findSkillPath(Skill.FIRE),
                (firstSkillX() + getFireBallSetting()[0]),
                getFireBallSetting()[1],
                getFireBallSetting()[2]);
        // Blitzschlag
        skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.LIGHTNING);
        setupSkill(
                findSkillPath(Skill.LIGHTNING),
                secondSkillX() + getLightningSetting()[0],
                getLightningSetting()[1],
                getLightningSetting()[2]);
        addLightningTimer();

        // Transform
        skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.TRANSFORM);
        setupSkill(
                findSkillPath(Skill.TRANSFORM),
                thirdSkillX() + getTransformSetting()[0],
                getTransformSetting()[1],
                getTransformSetting()[2]);
        addTransformTimer();

        // MindControl
        skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.MIND_CONTROL);
        setupSkill(
                findSkillPath(Skill.MIND_CONTROL),
                fourtSkillX() - getMindControlSetting()[0],
                getMindControlSetting()[1],
                getMindControlSetting()[2]);

        // Boomerang
        skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.BOOMERANG);
        setupSkill(
                findSkillPath(Skill.BOOMERANG),
                fiveSkillX() + getBoomerangSetting()[0],
                getBoomerangSetting()[1],
                getBoomerangSetting()[2]);
        addBoomerangTimer();

        // arrow
        skillSlots.put(Setup_Skill.SIX_SKILL, Skill.ARROW);
        setupSkill(
                findSkillPath(Skill.ARROW),
                sixSkillX() + getArrowSetting()[0],
                getArrowSetting()[1],
                getArrowSetting()[2]);
        addArrowTimer();

        addFKeys(); // F-Tasten beschriftung anzeigen
    }

    /*
       F-Tasten werden oben links angezeigt.
    */
    private void addFKeys() {
        for (int i = 1; i <= sizeOfSkills; i++) {
            ScreenText text = new ScreenText("F" + i, new Point(0, 0), 0);
            timerFontBuilder.setSize(11);
            timerFontBuilder.setFontColor(Color.NAVY);
            text.setStyle(new LabelStyleBuilder(timerFontBuilder.build()).build());
            text.getColor().a = 1f;
            if (i == 1) {
                text.setPosition(firstSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            } else if (i == 2) {
                text.setPosition(secondSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            } else if (i == 3) {
                text.setPosition(thirdSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            } else if (i == 4) {
                text.setPosition(fourtSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            } else if (i == 5) {
                text.setText("T");
                text.setPosition(fiveSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            } else if (i == 6) {
                text.setText("R");
                text.setPosition(sixSkillX() + 2, getBackgroundSkill().getHeight() + 8);
            }
            add((T) text);
        }
    }

    /*
       Timer mittig wird angezeigt bsp. 0/7.
       Aktualisiert sich nach eine Sekunde.
    */
    private void addLightningTimer() {
        txtLightningTimer = new ScreenText("", new Point(0, 0), 1.5f);
        txtLightningTimer.setPosition(
                secondSkillX() + getLightningSetting()[0], getBackgroundSkill().getHeight() + 10);
        timerFontBuilder.setFontColor(Color.WHITE);
        txtLightningTimer.setStyle(new LabelStyleBuilder(timerFontBuilder.build()).build());
        add((T) txtLightningTimer);
    }

    /*
       Timer mittig wird angezeigt bsp. 0/7.
       Aktualisiert sich nach eine Sekunde.
    */
    private void addTransformTimer() {
        txtTransformTimer = new ScreenText("", new Point(0, 0), 1.5f);
        txtTransformTimer.setPosition(
                thirdSkillX() + getTransformSetting()[0], getBackgroundSkill().getHeight() + 10);
        timerFontBuilder.setFontColor(Color.WHITE);
        txtTransformTimer.setStyle(new LabelStyleBuilder(timerFontBuilder.build()).build());
        add((T) txtTransformTimer);
    }

    /*
       Timer mittig wird angezeigt bsp. 0/7.
       Aktualisiert sich nach eine Sekunde.
    */
    private void addBoomerangTimer() {
        txtBoomerangTimer = new ScreenText("", new Point(0, 0), 1.5f);
        txtBoomerangTimer.setPosition(
                fiveSkillX() + getBoomerangSetting()[0], getBackgroundSkill().getHeight() + 10);
        timerFontBuilder.setFontColor(Color.WHITE);
        txtBoomerangTimer.setStyle(new LabelStyleBuilder(timerFontBuilder.build()).build());
        add((T) txtBoomerangTimer);
    }

    /*
       Timer mittig wird angezeigt bsp. 0/7.
       Aktualisiert sich nach eine Sekunde.
    */
    private void addArrowTimer() {
        txtArrowTimer = new ScreenText("", new Point(0, 0), 1.5f);
        txtArrowTimer.setPosition(
                sixSkillX() + getArrowSetting()[0], getBackgroundSkill().getHeight() + 10);
        timerFontBuilder.setFontColor(Color.WHITE);
        txtArrowTimer.setStyle(new LabelStyleBuilder(timerFontBuilder.build()).build());
        add((T) txtArrowTimer);
    }

    /*
       Die Methode löscht beide Images im Skill Slots
    */
    private void removeSkillSlotImage(Setup_Skill skill1, Setup_Skill skill2) {
        for (ScreenImage image : images) {
            if (image.getName().equalsIgnoreCase(findSkillPath(skill1))) {
                remove((T) image);
            }
            if (image.getName().equalsIgnoreCase(findSkillPath(skill2))) {
                remove((T) image);
            }
        }
    }

    /*
       Eigentliche austausch der Skill Slot
    */
    private void switchSkillSlot(Setup_Skill skill1, Setup_Skill skill2) {
        String now = findSkillPath(skill1);
        String prev = findSkillPath(skill2);
        String nowPosition = skill2.name();
        String prevPosition = skill1.name();

        switchSkillKey(now, prev, nowPosition, prevPosition);

        // Derzeitiges Skill
        if (now.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getFireBallSetting()[0]),
                    getFireBallSetting()[1],
                    getFireBallSetting()[2]);
        } else if (now.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            if (hero.getLevel() <= 3) {
                removeDisabledImages(Skill.LIGHTNING);
                addDisableBackground(
                        Skill.LIGHTNING.name(),
                        findSkillXPosition(nowPosition) + getLightningSetting()[0]);
            }
            if (hero.getLevel() >= 3) {
                removeDisabledImages(Skill.LIGHTNING);
            }
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getLightningSetting()[0]),
                    getLightningSetting()[1],
                    getLightningSetting()[2]);
        } else if (now.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            if (hero.getLevel() <= 10) {
                removeDisabledImages(Skill.TRANSFORM);
                addDisableBackground(
                        Skill.TRANSFORM.name(),
                        findSkillXPosition(nowPosition) + getTransformSetting()[0]);
            }
            if (hero.getLevel() >= 10) {
                removeDisabledImages(Skill.TRANSFORM);
            }
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getTransformSetting()[0]),
                    getTransformSetting()[1],
                    getTransformSetting()[2]);
        } else if (now.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            if (hero.getLevel() <= 15) {
                removeDisabledImages(Skill.MIND_CONTROL);
                addDisableBackground(
                        Skill.MIND_CONTROL.name(),
                        findSkillXPosition(nowPosition) + getMindControlSetting()[0]);
            }
            if (hero.getLevel() >= 15) {
                removeDisabledImages(Skill.MIND_CONTROL);
            }
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getMindControlSetting()[0]),
                    getMindControlSetting()[1],
                    getMindControlSetting()[2]);
        } else if (now.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getBoomerangSetting()[0]),
                    getBoomerangSetting()[1],
                    getBoomerangSetting()[2]);
        } else if (now.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            setupSkill(
                    now,
                    (findSkillXPosition(nowPosition) + getArrowSetting()[0]),
                    getArrowSetting()[1],
                    getArrowSetting()[2]);
        } else {
            UI_LOGGER.info("Tauschplatz nicht gefunden!");
        }

        // Vorheriger Skill
        if (prev.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getFireBallSetting()[0]),
                    getFireBallSetting()[1],
                    getFireBallSetting()[2]);
        } else if (prev.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            if (hero.getLevel() <= 3) {
                removeDisabledImages(Skill.LIGHTNING);
                addDisableBackground(
                        Skill.LIGHTNING.name(),
                        findSkillXPosition(nowPosition) + getLightningSetting()[0]);
            }
            if (hero.getLevel() >= 3) {
                removeDisabledImages(Skill.LIGHTNING);
            }
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getLightningSetting()[0]),
                    getLightningSetting()[1],
                    getLightningSetting()[2]);
        } else if (prev.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            if (hero.getLevel() <= 10) {
                removeDisabledImages(Skill.TRANSFORM);
                addDisableBackground(
                        Skill.TRANSFORM.name(),
                        findSkillXPosition(nowPosition) + getTransformSetting()[0]);
            }
            if (hero.getLevel() >= 10) {
                removeDisabledImages(Skill.TRANSFORM);
            }
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getTransformSetting()[0]),
                    getTransformSetting()[1],
                    getTransformSetting()[2]);
        } else if (prev.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            if (hero.getLevel() <= 15) {
                removeDisabledImages(Skill.MIND_CONTROL);
                addDisableBackground(
                        Skill.MIND_CONTROL.name(),
                        findSkillXPosition(nowPosition) + getMindControlSetting()[0]);
            }
            if (hero.getLevel() >= 15) {
                removeDisabledImages(Skill.MIND_CONTROL);
            }
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getMindControlSetting()[0]),
                    getMindControlSetting()[1],
                    getMindControlSetting()[2]);
        } else if (prev.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getBoomerangSetting()[0]),
                    getBoomerangSetting()[1],
                    getBoomerangSetting()[2]);
        } else if (prev.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            setupSkill(
                    prev,
                    (findSkillXPosition(prevPosition) + getArrowSetting()[0]),
                    getArrowSetting()[1],
                    getArrowSetting()[2]);
        } else {
            UI_LOGGER.info("Tausch vorgang konnte nicht durchgeführt werden!");
        }
    }

    /*
       Die im Map gespeicherten Keys und Values werden hier geändert,
       sobald switchSkill stattfindet.
    */
    private void switchSkillKey(String now, String prev, String nowPosition, String prevPosition) {

        if (nowPosition.equalsIgnoreCase(Setup_Skill.FIRST_SKILL.name())) {
            first(now);
        } else if (nowPosition.equalsIgnoreCase(Setup_Skill.SECOND_SKILL.name())) {
            second(now);
        } else if (nowPosition.equalsIgnoreCase(Setup_Skill.THIRD_SKILL.name())) {
            third(now);
        } else if (nowPosition.equalsIgnoreCase(Setup_Skill.FOURT_SKILL.name())) {
            fourt(now);
        } else if (nowPosition.equalsIgnoreCase(Setup_Skill.FIVE_SKILL.name())) {
            five(now);
        } else if (nowPosition.equalsIgnoreCase(Setup_Skill.SIX_SKILL.name())) {
            six(now);
        }

        if (prevPosition.equalsIgnoreCase(Setup_Skill.FIRST_SKILL.name())) {
            first(prev);
        } else if (prevPosition.equalsIgnoreCase(Setup_Skill.SECOND_SKILL.name())) {
            second(prev);
        } else if (prevPosition.equalsIgnoreCase(Setup_Skill.THIRD_SKILL.name())) {
            third(prev);
        } else if (prevPosition.equalsIgnoreCase(Setup_Skill.FOURT_SKILL.name())) {
            fourt(prev);
        } else if (prevPosition.equalsIgnoreCase(Setup_Skill.FIVE_SKILL.name())) {
            five(prev);
        } else if (prevPosition.equalsIgnoreCase(Setup_Skill.SIX_SKILL.name())) {
            six(prev);
        }
    }

    /*
       Es werden alle Skills durchgegangen um zu überprüfen,
       welche switchskill bzw. if-Abfrage getätigt wird.
       Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void first(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.FIRST_SKILL, Skill.ARROW);
        } else {
            UI_LOGGER.warning("Erste Skill konnte nicht eingefügt werden.");
        }
    }
    /*
        Es werden alle Skills durchgegangen um zu überprüfen,
        welche switchskill bzw. if-Abfrage getätigt wird.
        Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void second(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.SECOND_SKILL, Skill.ARROW);
        } else {
            UI_LOGGER.warning("Zweite Skill konnte nicht eingefügt werden.");
        }
    }
    /*
       Es werden alle Skills durchgegangen um zu überprüfen,
       welche switchskill bzw. if-Abfrage getätigt wird.
       Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void third(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.THIRD_SKILL, Skill.ARROW);
        } else {
            UI_LOGGER.warning("Dritte Skill konnte nicht eingefügt werden.");
        }
    }
    /*
       Es werden alle Skills durchgegangen um zu überprüfen,
       welche switchskill bzw. if-Abfrage getätigt wird.
       Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void fourt(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.FOURT_SKILL, Skill.ARROW);
        } else {
            UI_LOGGER.warning("Vierte Skill konnte nicht eingefügt werden.");
        }
    }
    /*
       Es werden alle Skills durchgegangen um zu überprüfen,
       welche switchskill bzw. if-Abfrage getätigt wird.
       Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void five(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.FIVE_SKILL, Skill.ARROW);
        } else {
            UI_LOGGER.warning("Fünfte Skill konnte nicht eingefügt werden.");
        }
    }
    /*
       Es werden alle Skills durchgegangen um zu überprüfen,
       welche switchskill bzw. if-Abfrage getätigt wird.
       Dann wird der Skill in der richtigen Position gesetzt.
    */
    private void six(String skillSwitch) {
        if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.FIRE))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.FIRE);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.LIGHTNING))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.LIGHTNING);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.TRANSFORM))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.TRANSFORM);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.MIND_CONTROL))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.MIND_CONTROL);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.BOOMERANG))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.BOOMERANG);
        } else if (skillSwitch.equalsIgnoreCase(findSkillPath(Skill.ARROW))) {
            skillSlots.put(Setup_Skill.SIX_SKILL, Skill.ARROW);
        }
    }

    /*
       Taste F1, welche Skill sollte ausgeführt werden, bzw.
       welche Taste welchem Skill gehört.
    */
    private void first(Object skill, Hero hero1) {
        if (skill == UI_HUD.Skill.FIRE) {
            // Skill 1 - Fireball
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                if (hero1.pc.getSkillSlot1().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot1().get());
                }
            }
        } else if (skill == UI_HUD.Skill.LIGHTNING && hero.getLevel() >= 3) {
            // Skill 2 - Blitzschlag
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                if (hero1.pc.getSkillSlot2().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot2().get());
                }
            }
        } else if (skill == UI_HUD.Skill.TRANSFORM && hero.getLevel() >= 10) {
            // Skill 3 - Verwandlung
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                if (hero1.pc.getSkillSlot3().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot3().get());
            }
        } else if (skill == UI_HUD.Skill.MIND_CONTROL && hero.getLevel() >= 15) {
            // Skill 4 Boss Informationen
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                if (hero1.pc.getSkillSlot4().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot4().get());
            }
        }
    }

    /*
       Taste F2, welche Skill sollte ausgeführt werden, bzw.
       welche Taste welchem Skill gehört.
    */
    private void second(Object skill, Hero hero1) {
        if (skill == UI_HUD.Skill.FIRE) {
            // Skill 1 - Fireball
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                if (hero1.pc.getSkillSlot1().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot1().get());
                }
            }
        } else if (skill == UI_HUD.Skill.LIGHTNING && hero.getLevel() >= 3) {
            // Skill 2 - Blitzschlag
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                if (hero1.pc.getSkillSlot2().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot2().get());
                }
            }
        } else if (skill == UI_HUD.Skill.TRANSFORM && hero.getLevel() >= 10) {
            // Skill 3 - Verwandlung
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                if (hero1.pc.getSkillSlot3().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot3().get());
            }
        } else if (skill == UI_HUD.Skill.MIND_CONTROL && hero.getLevel() >= 15) {
            // Skill 4 Boss Informationen
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                if (hero1.pc.getSkillSlot4().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot4().get());
            }
        }
    }

    /*
       Taste F3, welche Skill sollte ausgeführt werden, bzw.
       welche Taste welchem Skill gehört.
    */
    private void third(Object skill, Hero hero1) {
        if (skill == UI_HUD.Skill.FIRE) {
            // Skill 1 - Fireball
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                if (hero1.pc.getSkillSlot1().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot1().get());
                }
            }
        } else if (skill == UI_HUD.Skill.LIGHTNING && hero.getLevel() >= 3) {
            // Skill 2 - Blitzschlag
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                if (hero1.pc.getSkillSlot2().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot2().get());
                }
            }
        } else if (skill == UI_HUD.Skill.TRANSFORM && hero.getLevel() >= 10) {
            // Skill 3 - Verwandlung
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                if (hero1.pc.getSkillSlot3().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot3().get());
            }
        } else if (skill == UI_HUD.Skill.MIND_CONTROL && hero.getLevel() >= 15) {
            // Skill 4 Boss Informationen
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                if (hero1.pc.getSkillSlot4().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot4().get());
            }
        }
    }
    /*
       Taste F4, welche Skill sollte ausgeführt werden, bzw.
       welche Taste welchem Skill gehört.
    */
    private void fourt(Object skill, Hero hero1) {
        if (skill == UI_HUD.Skill.FIRE) {
            // Skill 1 - Fireball
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                if (hero1.pc.getSkillSlot1().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot1().get());
                }
            }
        } else if (skill == UI_HUD.Skill.LIGHTNING && hero.getLevel() >= 3) {
            // Skill 2 - Blitzschlag
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                if (hero1.pc.getSkillSlot2().isPresent()) {
                    hero1.execute(hero1.pc.getSkillSlot2().get());
                }
            }
        } else if (skill == UI_HUD.Skill.TRANSFORM && hero.getLevel() >= 10) {
            // Skill 3 - Verwandlung
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                if (hero1.pc.getSkillSlot3().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot3().get());
            }
        } else if (skill == UI_HUD.Skill.MIND_CONTROL && hero.getLevel() >= 15) {
            // Skill 4 Boss Informationen
            if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                if (hero1.pc.getSkillSlot4().isPresent())
                    hero1.execute(hero1.pc.getSkillSlot4().get());
            }
        }
    }

    /**
     * Skills und jeweiligen Slots werden initialisiert!
     *
     * @param hero1 Hero Klasse wird benötigt, um Skills zu aktivieren.
     */
    public void getSkills(Hero hero1) {
        this.hero = hero1;
        Iterator it = skillSlots.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            if (entry.getKey() == UI_HUD.Setup_Skill.FIRST_SKILL) {
                first(entry.getValue(), hero1);
            } else if (entry.getKey() == UI_HUD.Setup_Skill.SECOND_SKILL) {
                second(entry.getValue(), hero1);
            } else if (entry.getKey() == UI_HUD.Setup_Skill.THIRD_SKILL) {
                third(entry.getValue(), hero1);
            } else if (entry.getKey() == UI_HUD.Setup_Skill.FOURT_SKILL) {
                fourt(entry.getValue(), hero1);
            }
        }
    }

    /*
       Mit der Skill Name kann ich überprüfen welche skill es ist, um die x position herauszufinden.
    */
    private float findSkillXPosition(String position) {
        if (position.equalsIgnoreCase(Setup_Skill.FIRST_SKILL.name())) {
            return firstSkillX();
        } else if (position.equalsIgnoreCase(Setup_Skill.SECOND_SKILL.name())) {
            return secondSkillX();
        } else if (position.equalsIgnoreCase(Setup_Skill.THIRD_SKILL.name())) {
            return thirdSkillX();
        } else if (position.equalsIgnoreCase(Setup_Skill.FOURT_SKILL.name())) {
            return fourtSkillX();
        } else if (position.equalsIgnoreCase(Setup_Skill.FIVE_SKILL.name())) {
            return fiveSkillX();
        } else if (position.equalsIgnoreCase(Setup_Skill.SIX_SKILL.name())) {
            return sixSkillX();
        } else {
            return 0;
        }
    }

    /*
       Skill path herausfinden, durch enum Skill
    */
    private String findSkillPath(Skill skill) {
        switch (skill) {
            case FIRE -> {
                return PATH_TO_SKILLS[0];
            }
            case LIGHTNING -> {
                return PATH_TO_SKILLS[1];
            }
            case TRANSFORM -> {
                return PATH_TO_SKILLS[2];
            }
            case MIND_CONTROL -> {
                return PATH_TO_SKILLS[3];
            }
            case BOOMERANG -> {
                return PATH_TO_SKILLS[4];
            }
            case ARROW -> {
                return PATH_TO_SKILLS[5];
            }
            default -> UI_LOGGER.warning("Skill Path kann nicht ermittelt werden!");
        }
        return null;
    }

    /*
       Skill path herausfinden, durch enum Setup_Skill
    */
    private String findSkillPath(Setup_Skill skill) {
        switch (skill) {
            case FIRST_SKILL -> {
                return PATH_TO_SKILLS[0];
            }
            case SECOND_SKILL -> {
                return PATH_TO_SKILLS[1];
            }
            case THIRD_SKILL -> {
                return PATH_TO_SKILLS[2];
            }
            case FOURT_SKILL -> {
                return PATH_TO_SKILLS[3];
            }
            case FIVE_SKILL -> {
                return PATH_TO_SKILLS[4];
            }
            case SIX_SKILL -> {
                return PATH_TO_SKILLS[5];
            }
        }
        return "";
    }

    /*
       Skill initialisierung.
       Hier kann man ein Skill Hinzufügen.
       Position hängt von x ab! Deshalb es gibt die Methoden (firstSKillX-sixSkillX)
    */
    private void setupSkill(String skillPath, float x, float y, float scale) {
        addSkill(skillPath, x, y, scale);
    }

    // Hilfsmethoden SkillPosition
    private static final float SKILL_POSITION = 35;
    /*
        Skill position 1
    */
    private float firstSkillX() {
        return ((Constants.WINDOW_WIDTH / 2f) / 2f) + SKILL_POSITION;
    }
    /*
        Skill position 2
    */
    private float secondSkillX() {
        return firstSkillX() + 35;
    }
    /*
        Skill position 3
    */
    private float thirdSkillX() {
        return secondSkillX() + 35;
    }
    /*
        Skill position 4
    */
    private float fourtSkillX() {
        return (Constants.WINDOW_WIDTH / 2f) + 35;
    }
    /*
        Skill position 5
    */
    private float fiveSkillX() {
        return fourtSkillX() + 35;
    }
    /*
       Skill position 6
    */
    private float sixSkillX() {
        return fiveSkillX() + 35;
    }

    /*
     * BackgroundImage wird zurückgeworfen.
     */
    private ScreenImage getBackgroundSkill() {
        return new ScreenImage(PATH_TO_SKILL_BACKGROUND, new Point(0, 0));
    }

    /**
     * Hintergrund hinzufügen
     *
     * @param position erwartet einen float, um den Hintergrund zu positionieren.
     */
    private void addBackground(float position) {
        backgroundPosition(getBackgroundSkill(), position, 10, 2f);
    }

    /*
       Fügt ein Graue hintergrund
    */
    private ScreenImage addDisableBackground(String name, float position) {
        ScreenImage disable = new ScreenImage(PATH_TO_BACKGROUND_DISABLED, new Point(0, 0));
        disable.setName(name);
        disable.getColor().a = 0.6f;
        backgroundPosition(disable, position, 10, 2f);
        imagesDisable.add(disable);
        return disable;
    }

    /*
      Skill wird positioniert.
    */
    private void addSkill(
            String imagePath, float imagePositionX, float imagePositionY, float imageScale) {
        ScreenImage image = new ScreenImage(imagePath, new Point(0, 0));
        image.setName(imagePath);
        skillPosition(
                image, // ScreenImage background & skill image
                imagePositionX, // Skill Position x
                10 + (getBackgroundSkill().getHeight() / 2f) - imagePositionY, // Skill Position y
                imageScale); // Image scale
    }

    /*
       Background für skill.
       Der Position wäre mittig: 3 Skill Links, 3 Skill Rechts
    */
    private void backgroundPosition(
            ScreenImage imageBackground,
            float xBackground,
            float yBackground,
            float backgroundScale) {
        imageBackground.setScale(backgroundScale);
        // Background position
        Point pointBackground = new Point(xBackground, yBackground);
        imageBackground.setPosition(pointBackground.x, pointBackground.y);
        add((T) imageBackground);
    }

    /* Skill Einstellung für Fireball */
    private float[] getFireBallSetting() {
        return new float[] {2, 5, 2};
    }

    /* Skill Einstellung für Lightning */
    private float[] getLightningSetting() {
        return new float[] {1, -2.5f, .3f};
    }

    /* Skill Einstellung für Transform */
    private float[] getTransformSetting() {
        return new float[] {4, 4, .4f};
    }

    /* Skill Einstellung für MindControl */
    private float[] getMindControlSetting() {
        return new float[] {2, 8, 1};
    }

    /* Skill Einstellung für Boomerang */
    private float[] getBoomerangSetting() {
        return new float[] {6, 0, .3f};
    }

    /* Skill Einstellung für Arrow */
    private float[] getArrowSetting() {
        return new float[] {6, 2, .3f};
    }

    /*
       Skill wird mittig auf dem Background angezeigt.
    */
    private void skillPosition(ScreenImage image, float xImage, float yImage, float imageScale) {

        image.setScale(imageScale);

        // Image Position
        Point pointImage = new Point(xImage, yImage);
        // Einstellung
        image.setPosition(pointImage.x, pointImage.y);

        // Hinzufügen
        add((T) image);
        images.add(image);
    }

    /*
       Hilfsmethode um zu überprüfen, welche skill aktiviert, getauscht oder versteckt wird.
    */
    private enum Skill {
        FIRE,
        LIGHTNING,
        TRANSFORM,
        MIND_CONTROL,
        BOOMERANG,
        ARROW
    }

    /** Um zu überprüfen, welche skill zu welchem slot gehört. */
    public enum Setup_Skill {
        FIRST_SKILL,
        SECOND_SKILL,
        THIRD_SKILL,
        FOURT_SKILL,
        FIVE_SKILL,
        SIX_SKILL
    }
}
