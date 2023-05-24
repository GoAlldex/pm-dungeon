package ecs.components.skill;

import ecs.components.Component;
import ecs.entities.Entity;
import java.util.Random;

/**
 *  ManaComponent
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 22.05.2023
 */
public class ManaComponent extends Component {

    private int maxManaPoint = 100; //maximale mana punkte
    private int currentManaPoint; // aktuelle mana punkte
    private int frames; //frames
    private int currentFrame; //aktuelle frame
    private boolean generateManaPoint; //mana point generierens
    //Timer für mana regeneration
    private long timerStart = System.currentTimeMillis();

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     */
    public ManaComponent(Entity entity, int currentManaPoint, int frames) {
        super(entity);
        this.currentManaPoint = currentManaPoint;
        this.frames = frames;
        this.currentFrame = frames;
        this.generateManaPoint = true;
    }

    /**
     * Mana Punkte werden sich nach einer gewissen Zeit regenerieren
     * @param i Anzahl der zunehmende Punkt
     */
    private void regenerateManaPoint(int i){
        currentManaPoint = Math.min(maxManaPoint, currentManaPoint + i);
    }


    /**
     * Generiert 1 Mana Punkt
     * Zwischen 3s - 10s regenerieren sich die Mana punkte
     * bis zum maximalen Punkte.
     */
    public void generateManaPoints(){
        int min = 5;
        int max = 20;
        int val = new Random().nextInt(max - min + 1) + min;
        currentFrame--;
        if (currentFrame <= 0 && generateManaPoint){
            long timerEnd = System.currentTimeMillis();
            if ((timerEnd - timerStart) / (60*60) == val){
                regenerateManaPoint(1);
                currentFrame = frames;
                System.out.println("ManaPoints: " + currentManaPoint);
                timerStart = System.currentTimeMillis();
            }
        }
    }

    /**
     * Wenn der Held ein neues Level erreicht,
     * werden die Mana Punkte um 20% regeneriert!
     * und die Maximalen Mana Punkte werden ebenso erhöht: 10%
     */
    public void generateManaPointToNextLevel(){
        int pointToNextLevel = (maxManaPoint * 10) / 100;
        currentManaPoint = currentManaPoint + ((maxManaPoint * 20) / 100);
        maxManaPoint = Math.max(maxManaPoint, maxManaPoint + pointToNextLevel);
    }

    /**
     * @return Werfe maximale Mana Point zurück
     */
    public int getMaxManaPoint() {
        return maxManaPoint;
    }

    /**
     * @return Werfe aktuellen Mana Point zurück
     */
    public int getCurrentManaPoint() {
        return currentManaPoint;
    }

    /**
     * Wenn der Spieler Mana Point verliert,
     * kann es über diese Methode gesetzt werden.
     * @param point Wie viel Mana Point der Spieler verliert
     */
    public void setCurrentManaPoint(int point){
        this.currentManaPoint = point;
    }
}
