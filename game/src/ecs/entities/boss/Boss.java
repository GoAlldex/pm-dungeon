package ecs.entities.boss;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.Skill;
import ecs.entities.Monster;
import graphic.Animation;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 *
 *
 * <h1>Boss</h1>
 *
 * <h2>Grundklasse für alle Bossmonstern</h2>
 *
 * <h3>Methoden:</h3>
 *
 * <h4>{@link #setupIIdleAI()}
 *
 * <p>Boss strategy initialisiere. </h4>
 *
 * <h4>{@link #bossPosition(PositionComponent)}
 *
 * <p>Boss position. </h4>
 *
 * <h4>{@link #setupVelocityComponent()}
 *
 * <p>Renntextur </h4>
 *
 * <h4>{@link #setupAnimationComponent()}
 *
 * <p>Stehtextur </h4>
 *
 * <h4>{@link #setupSkills()}
 *
 * <p>Skills initialisieren </h4>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version 1.0
 */
public abstract class Boss extends Monster {

    // Variablen
    protected PositionComponent bossPosition; // Boss Position
    private static LinkedList<Skill> skills = new LinkedList<>(); // weitere Skills
    protected Skill skill1, skill2; // default
    private int level = 0; // Held level
    protected Logger bossLogger;

    /**
     * Konstruktor
     *
     * @param level Held aktuelle level
     */
    public Boss(int level) {
        this.level = level;
        this.bossLogger = Logger.getLogger(getClass().getName());
    }

    /** Boss eigenartiges Strategy */
    protected void setupIIdleAI() {}

    /** Renn Animation Textur */
    protected void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, getSpeed()[0], getSpeed()[1], moveLeft, moveRight);
    }

    /** Steh Animation Textur */
    protected void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    /** Boss skills initialisieren */
    protected void setupSkills() {}

    /**
     * @return liefert das aktuelle level von dem Held
     */
    protected int getLevel() {
        return level;
    }

    /**
     * Boss position
     *
     * @param entity Boss entity
     * @return new PositionComponent für Boss
     */
    protected PositionComponent bossPosition(PositionComponent entity) {
        this.bossPosition = entity;
        return new PositionComponent(bossPosition.getEntity());
    }

    /**
     * @return liefert aktuelle Boss Position
     */
    public PositionComponent getBossPosition() {
        return new PositionComponent(this);
    }

    /**
     * Falls, der Boss mehr Skills hat
     *
     * @param skill new Skill
     */
    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    /**
     * Löscht ein unerwünschtes Skill
     *
     * @param skill Skill der gelöscht werden soll
     * @return true der Skill wurde gelöscht, false ansonsten gibt es Probleme.
     */
    public boolean removeSkill(Skill skill) {
        return skills.remove(skill);
    }

    /**
     * Iteriere über alle Skills
     *
     * @param i indexwert
     * @return gewünschtes Skill
     */
    public Skill get(int i) {
        return skills.get(i);
    }

    /**
     * @return liefert Anzahl an Skills.
     */
    public int size() {
        return skills.size();
    }

    public String getAnimation(String name) {
        switch (name) {
            case "left" -> {
                return pathToIdleLeft;
            }
            case "right" -> {
                return pathToIdleRight;
            }
            case "runLeft" -> {
                return pathToRunLeft;
            }
            case "runRight" -> {
                return pathToRunRight;
            }
        }
        return "";
    }

    /**
     * @return Wirft ein empty Animation file zurück.
     */
    public Animation getEmptyAnimation() {
        return AnimationBuilder.buildAnimation("monster/empty/empty_f0.png");
    }

    public abstract String information();
}
