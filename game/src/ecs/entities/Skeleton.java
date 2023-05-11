package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.Idle;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.idle.StaticRadiusWalk;
import ecs.items.ItemDataGenerator;
import graphic.Animation;

import java.util.Random;

/**
 <b><span style="color: rgba(3,71,134,1);">Unsere Monsterklasse "Skelett".</span></b><br>
 Hier werden die wichtigesten bestandteile unseres Monsters "Skelett" initialisiert.<br><br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_2
 @since 10.05.2023
 */
public class Skeleton extends Monster {

    /**
     <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     Initialisiert ein neues Monster "Skelett".
     @param level Typ des Monsters
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 10.05.2023
     */
    public Skeleton(int level) {
        super();
        this.position = new PositionComponent(this);
        onDeath();
        this.hp = new HealthComponent(this, Math.round(25f*(1f+((float)level/10f)-0.1f)), this.death, null, null);
        this.xp = Math.round(10*(1+(level/10)-0.1f));
        this.dmg = Math.round(2*(1+(level/10)-0.1f));
        this.dmgType = 0;
        this.speed[0] = 0.3f;
        this.speed[1] = 0.3f;
        this.pathToIdleLeft = "monster/type4/idleLeft";
        this.pathToIdleRight = "monster/type4/idleRight";
        this.pathToRunLeft = "monster/type4/runLeft";
        this.pathToRunRight = "monster/type4/runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        this.ai = new AIComponent(this);
        this.ai.execute();
        setItem();
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
            (you, other, direction) -> System.out.println("SkelettCollisionEnter"),
            (you, other, direction) -> System.out.println("SkelettCollisionLeave"));
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
     @version cycle_2
     @since 10.05.2023
     */
    public void onDeath() {
        this.death = new IOnDeathFunction() {
            @Override
            public void onDeath(Entity entity) {

            }
        };
    }

    public void setToTomb(PositionComponent position) {
        this.position = new PositionComponent(this, position.getPosition());
    }

}
