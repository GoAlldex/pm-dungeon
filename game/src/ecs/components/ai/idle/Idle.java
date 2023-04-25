package ecs.components.ai.idle;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Constants;
import tools.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Idle implements IIdleAI {

    private static final Random random = new Random();

    public enum MODE {
        /** Stand still */
        IDLE
    }

    private final MODE mode;
    private boolean initialized = false;

    /**
     * Idle Stays still
     */
    public Idle(MODE mode) {
        this.mode = mode;
    }

    private void init(Entity entity) {
        initialized = true;
        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Point center = position.getPosition();
        Tile tile = Game.currentLevel.getTileAt(position.getPosition().toCoordinate());

        if (tile == null) {
            return;
        }
    }

    @Override
    public void idle(Entity entity) {
        if (!initialized) this.init(entity);

        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
    }
}
