package ecs.systems;

import ecs.components.skill.ManaComponent;
import starter.Game;

/**
 * ManaSystem
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 22.05.2023
 */
public class ManaSystem extends ECS_System{
    /**
     * Iteriere auf alle Component die ein ManaComponent besitzen
     */
    @Override
    public void update() {
        Game.getEntities().stream()
            .flatMap(e -> e.getComponent(ManaComponent.class).stream())
            .forEach(mc -> ((ManaComponent) mc).generateManaPoints());
    }
}
