package ecs.components.skill;

import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.entities.boss.Boss;
import java.util.logging.Logger;

/**
 * MindControlSkill
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 24.05.2023
 */
public class MindControlSkill implements ISkillFunction {
    private boolean fight; // ist der entity am Kämpfen
    private int mana; // Abziehen der mana
    private int hp; // wie viel hp kostet der MindControlSkill
    private Hero hero;
    private Entity entity;
    private boolean requiredLevel = false;
    private final Logger mindControlLogger = Logger.getLogger(getClass().getName());

    /** Konstruktor */
    public MindControlSkill() {}

    /**
     * @param entity which uses the skill
     */
    @Override
    public void execute(Entity entity) {
        hero = (Hero) entity;
        // Abfrage ob level > 15 ist
        if (hero.getLevel() >= 15) {
            this.mana = Math.round(2f * hero.getLevel());
            this.hp = Math.round(0.2f * hero.getLevel() + 1);
            if (isFight()) {
                Hero hero = (Hero) entity;
                int manaT = hero.getMc().getCurrentManaPoint() - mana; // temp
                if (hero.getMc().getCurrentManaPoint() > 0 && manaT > 0) {
                    hero.getMc().setCurrentManaPoint(manaT);
                    hero.getHp().setCurrentHealthpoints(hero.getHp().getCurrentHealthpoints() - hp);
                    mindControlLogger.info(
                            "MindControl kostet ("
                                    + mana
                                    + ") und Hero hat "
                                    + hero.getMc().getCurrentManaPoint()
                                    + " mana.");
                    mindControlLogger.info("Zusaetzlich werden " + hp + "hp abgezogen!");
                    mindControlLogger.info(information());
                }

                if (manaT < 0 && hero.getHp().getCurrentHealthpoints() >= 0) {
                    hero.getHp()
                            .setCurrentHealthpoints(
                                    hero.getHp().getCurrentHealthpoints() - (-manaT));
                    mindControlLogger.info(
                            "Mana sind alle deshalb werden von hp abgezogen!"
                                    + "\nMindControl kostet ("
                                    + mana
                                    + ")"
                                    + ", zusaetzlich werden "
                                    + hp
                                    + "hp abgezogen!"
                                    + "\nInsgesamt: "
                                    + (manaT + hp)
                                    + "hp wurden abgezogen!(Es werden "
                                    + "auch vorhandenen Mana abgezogen!)");
                    mindControlLogger.info(information());
                }
            }
            requiredLevel = true;
        } else {
            mindControlLogger.info(
                    "Dein Level ist zu niedrig!!! (" + getClass().getSimpleName() + ")");
            requiredLevel = true;
        }
    }

    /**
     * @return Boss Informationen
     */
    public String information() {
        Boss boss = (Boss) entity;
        return boss.information();
    }

    /**
     * @param fight true or false (sind es in einem Kampf)
     */
    public void setFight(boolean fight) {
        this.fight = fight;
    }

    public boolean isFight() {
        return fight;
    }

    public int getMana() {
        return mana;
    }

    public void setOther(Entity entity) {
        this.entity = entity;
    }

    public boolean isRequiredLevel() {
        return requiredLevel;
    }
}
