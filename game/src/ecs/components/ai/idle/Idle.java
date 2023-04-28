package ecs.components.ai.idle;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

/**
 <b><span style="color: rgba(3,71,134,1);">Idle AI Strategie.</span></b><br>
 Hier wird der Idle Zustand einer Entity definiert.<br><br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_1
 @since 26.04.2023
 */
public class Idle implements IIdleAI {

    private boolean initialized = false;

    /**
     <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     Nicht benötigt, da sich die Entity passiv verhalten soll.<br><br>

     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
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
