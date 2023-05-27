package ecs.components.skill;

import ecs.entities.Entity;
import ecs.entities.Hero;
import starter.Game;
import tools.Constants;

public class Skill {

    private ISkillFunction skillFunction;
    private int coolDownInFrames;
    private int currentCoolDownInFrames;
    private int manaPoint;

    /**
     * @param skillFunction Function of this skill
     */
    public Skill(ISkillFunction skillFunction, float coolDownInSeconds, int manaPoint) {
        this.skillFunction = skillFunction;
        this.coolDownInFrames = (int) (coolDownInSeconds * Constants.FRAME_RATE);
        this.currentCoolDownInFrames = 0;
        this.manaPoint = manaPoint;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity entity which uses the skill
     */
    public void execute(Entity entity) {
        if (!isOnCoolDown() && entity != null) {
            skillFunction.execute(entity);
            if (Game.getHero().isPresent() && entity == Game.getHero().get()) {
                Hero hero = (Hero) entity;
                if (hero.requiredLevel()) {
                    if (manaPoint > 0) {
                        if (hero.getMc().getCurrentManaPoint() >= 0) {
                            hero.getMc()
                                    .setCurrentManaPoint(
                                            hero.getMc().getCurrentManaPoint() - manaPoint);
                            System.out.println("HeroMana: " + hero.getMc().getCurrentManaPoint());
                        }
                        if (hero.getHp().getCurrentHealthpoints() >= 0
                                && hero.getMc().getCurrentManaPoint() <= 0) {
                            hero.getHp()
                                    .setCurrentHealthpoints(
                                            hero.getHp().getCurrentHealthpoints() - manaPoint);
                            System.out.println("HeroHP: " + hero.getHp().getCurrentHealthpoints());
                            if (hero.getHp().getCurrentHealthpoints() <= 0) {
                                hero.gameOver();
                            }
                        }
                    }
                }

                if (manaPoint > 0 && !hero.requiredLevel()) {
                    if (hero.getMc().getCurrentManaPoint() >= 0) {
                        hero.getMc()
                                .setCurrentManaPoint(
                                        hero.getMc().getCurrentManaPoint() - manaPoint);
                        System.out.println("HeroMana: " + hero.getMc().getCurrentManaPoint());
                    }
                    if (hero.getHp().getCurrentHealthpoints() >= 0
                            && hero.getMc().getCurrentManaPoint() <= 0) {
                        hero.getHp()
                                .setCurrentHealthpoints(
                                        hero.getHp().getCurrentHealthpoints() - manaPoint);
                        System.out.println("HeroHP: " + hero.getHp().getCurrentHealthpoints());
                        if (hero.getHp().getCurrentHealthpoints() <= 0) {
                            hero.gameOver();
                        }
                    }
                }
            }
            activateCoolDown();
        }
    }

    /**
     * @return true if cool down is not 0, else false
     */
    public boolean isOnCoolDown() {
        return currentCoolDownInFrames > 0;
    }

    /** activate cool down */
    public void activateCoolDown() {
        currentCoolDownInFrames = coolDownInFrames;
    }

    /** reduces the current cool down by frame */
    public void reduceCoolDown() {
        currentCoolDownInFrames = Math.max(0, --currentCoolDownInFrames);
    }
}
