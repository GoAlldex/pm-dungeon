package ecs.components.skill;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;
import java.util.logging.Logger;

/**
 * HealSkill
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 22.05.2023
 */
public class HealSkill implements ISkillFunction {

    private HealthComponent entityHealth;
    public boolean isFull = false;
    private int healPoint;
    private static final int healPercent = 20;
    protected Logger healLogger = Logger.getLogger(HealSkill.class.getName());

    /**
     * Führe Heilen aus.
     *
     * @param entity which uses the skill
     */
    @Override
    public void execute(Entity entity) {
        entityHealth =
                (HealthComponent)
                        entity.getComponent(HealthComponent.class)
                                .orElseThrow(
                                        () ->
                                                new MissingComponentException(
                                                        "HealthComponent HealSkill"));
        if (entityHealth.getCurrentHealthpoints() != entityHealth.getMaximalHealthpoints()) {
            int hp = entityHealth.getCurrentHealthpoints();
            setHealPoint(hp + getAmount());
            healLogger.info(
                    getAmount()
                            + "HP wurden regeneriert! Jetzt sind ("
                            + (hp + getAmount())
                            + "hp)");
            isFull = false;
        } else {
            isFull = true;
            healLogger.info("HP sind voll!");
        }
    }

    /**
     * Regeneriere HP
     *
     * @return wie viel HP regenerierte werden kann
     */
    private int getAmount() {
        int amount;
        int currentHP = entityHealth.getCurrentHealthpoints();
        amount = (currentHP * healPercent) / 100;
        return amount;
    }

    /** Setze wie viel hp wiederherstellbar sind */
    private void setHealPoint(int heal) {
        this.healPoint = heal;
    }

    /**
     * @return Wie viel hp wurden wiederhergestellt!
     */
    public int getHeal() {
        return healPoint;
    }

    public int getHealPercent() {
        return healPercent;
    }
}
