package ecs.components.ai.idle;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

public class Idle implements IIdleAI {


    private boolean initialized = false;

    /**
     * Idle Stays still
     */
    public Idle() {}

    private void init(Entity entity) {
        initialized = true;
        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Point center = position.getPosition();
        Tile tile = Game.currentLevel.getTileAt(position.getPosition().toCoordinate());
        if(tile == null) {
            return;
        }
    }

    @Override
    public void idle(Entity entity) {
        if(!initialized) this.init(entity);
        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
    }
}
