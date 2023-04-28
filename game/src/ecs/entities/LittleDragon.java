package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.Idle;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.idle.StaticRadiusWalk;
import ecs.items.ItemData;
import ecs.items.ItemDataGenerator;
import graphic.Animation;

import java.util.Random;

/**
 <b><span style="color: rgba(3,71,134,1);">Unsere Monsterklasse "Kleiner Drache".</span></b><br>
 Hier werden die wichtigesten bestandteile unseres Monsters "Kleiner Drache" initialisiert.<br><br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_1
 @since 26.04.2023
 */
public class LittleDragon extends Monster {

    /**
     <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     Initialisiert ein neues Monster "Kleiner Drache".
     @param level Typ des Monsters
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public LittleDragon(int level) {
        super();
        this.position = new PositionComponent(this);
        this.hp = Math.round(40*(1+(level/10)-0.1f));
        this.xp = Math.round(30*(1+(level/10)-0.1f));
        this.dmg = Math.round(7*(1+(level/10)-0.1f));
        this.dmgType = 0;
        this.speed[0] = 0.2f;
        this.speed[1] = 0.2f;
        this.pathToIdleLeft = "monster/type3/idleLeft";
        this.pathToIdleRight = "monster/type3/idleRight";
        this.pathToRunLeft = "monster/type3/runLeft";
        this.pathToRunRight = "monster/type3/runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        this.ai = new AIComponent(this);
        monsterMoveStrategy();
        this.ai.execute();
        setItem();
    }

    private void monsterMoveStrategy() {
        Random rnd = new Random();
        int strategy = rnd.nextInt(4);
        int radius = rnd.nextInt(8)+2;
        int checkPoints = rnd.nextInt(3)+2;
        int pauseTime = rnd.nextInt(5)+1;
        switch(strategy) {
            case 0:
                this.ai.setIdleAI(new PatrouilleWalk(radius, checkPoints, pauseTime, PatrouilleWalk.MODE.LOOP));
                break;
            case 1:
                this.ai.setIdleAI(new Idle());
                break;
            case 2:
                this.ai.setIdleAI(new RadiusWalk(radius, pauseTime));
                break;
            case 3:
                this.ai.setIdleAI(new StaticRadiusWalk(radius, pauseTime));
                break;
        }
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, this.speed[0], this.speed[1], moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> System.out.println("LittleDragonCollisionEnter"),
            (you, other, direction) -> System.out.println("LittleDragonCollisionLeave"));
    }

    private void setItem() {
        ItemDataGenerator itm = new ItemDataGenerator();
        int rnd = new Random().nextInt(itm.getAllItems().size());
        this.item = itm.getItem(rnd);
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Monster Loot</span></b><br>
     Rückgabe Monster Loot
     @return ItemData Zufälliges Item
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public ItemData onDeath() {
        return this.item;
    }

}
