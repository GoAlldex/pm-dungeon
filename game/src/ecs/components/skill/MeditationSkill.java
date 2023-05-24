package ecs.components.skill;

import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;

import java.util.logging.Logger;

/**
 *  MeditationSkill
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 24.05.2023
 */
public class MeditationSkill implements ISkillFunction{

    private long shield;
    private HealthComponent entityHC;
    private Logger meditationLogger = Logger.getLogger(getClass().getName());

    @Override
    public void execute(Entity entity) {
        entityHC = (HealthComponent)
            entity.getComponent(HealthComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("HealthComponent")
                );
        if (entityHC.getCurrentHealthpoints() > 0){
            setShield(entityHC.getCurrentHealthpoints() * 2);
        }else {
            meditationLogger.info("Entity ist tod!");
        }
    }

    private void setShield(int shield) {
        this.shield = shield;
    }

    /**
     * Shield aktivieren
     * @return Shield wird zurückgeworfen
     *          (Die HP werden verdoppelt und darauf addiert)
     */
    public int shield(){
        if (isShieldActive()){
            return Math.round(entityHC.getCurrentHealthpoints() + shield);
        }
        return 0;
    }

    private boolean isShieldActive(){
        return shield > 0;
    }
}
