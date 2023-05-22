package ecs.entities;

import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.items.ItemData;
import ecs.systems.MyFormatter;

import java.io.IOException;
import java.util.logging.*;

/**
 <b><span style="color: rgba(3,71,134,1);">Unsere Grund Monsterklasse, die alle Monster erben.</span></b><br>
 Hier werden die wichtigesten bestandteile eines Monsters definiert.<br><br>

 Methoden die hier verwendet werden:<br>
 {@link #getHp()}<br>
 {@link #getXp()}<br>
 {@link #getDmg()}<br>
 {@link #getDmgType()}<br>
 {@link #getSpeed()}<br>
 {@link #getPosition()}<br>
 {@link #setPosition(PositionComponent)}<br>
 {@link #getAI()}<br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_1
 @since 26.04.2023
 */
public abstract class Monster extends Entity {

    protected int hp;
    protected HealthComponent hp2;
    protected long xp;
    protected int dmg;
    protected int dmgType;
    protected float[] speed = new float[2];
    protected boolean fight = false;
    protected PositionComponent position;
    protected String pathToIdleLeft;
    protected String pathToIdleRight;
    protected String pathToRunLeft;
    protected String pathToRunRight;
    protected AIComponent ai;
    protected ItemData item;
    protected boolean dropItem = false;
    protected IOnDeathFunction death;
    protected Hero hero = null;
    protected int hitSpeed;
    protected int hitPause = 0;
    protected int frameTime;

    protected static final Logger log = Logger.getLogger(Biter.class.getName());

    /**
     <b><span style="color: rgba(3,71,134,1);">Logger für das Monster Beißer</span></b><br>
     Loggen der Beißer  Ereignisse in der Datei Biter.txt im Ordner Logs.<br>

     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_3
     @since 21.05.2023
     */
    public static void MonsterLogs(){
        Handler fileHandler = null;
        try {
            fileHandler = new FileHandler("logs/log_Monster.txt",true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new MyFormatter("Monster"));
            log.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.WARNING);
        consoleHandler.setFormatter(new MyFormatter("Monster"));
        log.addHandler(consoleHandler);
        log.setUseParentHandlers(false);
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster HP Menge</span></b><br>
     Rückgabe der Monster HP
     @return int aktuelles Leben
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public int getHp() {
        return this.hp;
    }
    /*public int getHp() {
        return this.hp.getCurrentHealthpoints();
    }*/

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster XP Menge</span></b><br>
     Rückgabe der Monster XP
     @return int XP
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public long getXp() {
        return this.xp;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Schadens Menge</span></b><br>
     Rückgabe der Monster schadens Menge
     @return int aktueller Schaden
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public int getDmg() {
        return this.dmg;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Schadens Typ</span></b><br>
     Rückgabe des Monster schadens Typs Nah-/Fernkampf
     @return int aktueller Schadens Typ
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public int getDmgType() {
        return this.dmgType;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Geschwindigkeit</span></b><br>
     Rückgabe der Monster Geschwindigkeit als Array Index: 0,1
     @return float[] aktuelle Geschwindigkeit x,y
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public float[] getSpeed() {
        return this.speed;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Position (holen)</span></b><br>
     Rückgabe der Monster Position
     @return PositionComponent aktuelle Monster Position
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public PositionComponent getPosition() {
        return this.position;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Position (setzen)</span></b><br>
     Setzen der Monster Position im Dungeon
     @param position Neue Position des Monsters
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public void setPosition(PositionComponent position) {
        this.position = position;
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster AI Strategie</span></b><br>
     Rückgabe der Monster AI Strategie
     @return AIComponent aktuelle Monster AI Strategie
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public AIComponent getAI() {
        return this.ai;
    }

}
