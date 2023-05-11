package ecs.entities;

import ecs.components.PositionComponent;
import ecs.components.ai.AIComponent;
import ecs.items.ItemData;

/**
 <b><span style="color: rgba(3,71,134,1);">Unsere Grund NPC-Klasse, die alle NPCs erben.</span></b><br>
 Hier werden die wichtigesten bestandteile eines NPCs definiert.<br><br>

 Methoden die hier verwendet werden:<br>
 {@link #getSpeed()}<br>
 {@link #getPosition()}<br>
 {@link #setPosition(PositionComponent)}<br>
 {@link #getAI()}<br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_2
 @since 08.05.2023
 */
public abstract class NPC extends Entity {

    protected float[] speed = new float[2];
    protected PositionComponent position;
    protected String pathToIdleLeft;
    protected String pathToIdleRight;
    protected String pathToRunLeft;
    protected String pathToRunRight;
    protected AIComponent ai;

    /**
     <b><span style="color: rgba(3,71,134,1);">NPC Geschwindigkeit</span></b><br>
     Rückgabe der NPC Geschwindigkeit als Array Index: 0,1
     @return float[] aktuelle Geschwindigkeit x,y
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public float[] getSpeed() {
        return this.speed;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">NPC Position (holen)</span></b><br>
     Rückgabe der NPC Position
     @return PositionComponent aktuelle NPC Position
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public PositionComponent getPosition() {
        return this.position;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">NPC Position (setzen)</span></b><br>
     Setzen der NPC Position im Dungeon
     @param position Neue Position des NPCs
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public void setPosition(PositionComponent position) {
        this.position = position;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">NPC AI Strategie</span></b><br>
     Rückgabe der NPC AI Strategie
     @return AIComponent aktuelle NPC AI Strategie
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public AIComponent getAI() {
        return this.ai;
    }

}
