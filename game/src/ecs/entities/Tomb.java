package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.items.ItemData;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import graphic.Animation;
import java.util.Random;
import java.util.Set;

/**
 * <b><span style="color: rgba(3,71,134,1);">Unsere Grabstein-Klasse.</span></b><br>
 * Hier werden die wichtigesten bestandteile unseres Grabsteins initialisiert.<br>
 * <br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_2
 * @since 08.05.2023
 */
public class Tomb extends Entity {

    private String tombImg = "objects/tomb/";
    private PositionComponent position;
    private Ghost ghost;
    private boolean rewardOrPunishment = false;
    private boolean isRewarded = false;

    /**
     * <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     * Initialisiert ein neuen Grabstein.
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_2
     * @since 08.05.2023
     */
    public Tomb() {
        this.position = new PositionComponent(this);
        setupAnimationComponent();
        setupCollision();
    }

    public Tomb(Ghost ghost) {
        this.ghost = ghost;
        this.position = new PositionComponent(this);
        setupAnimationComponent();
        setupCollision();
    }

    private void setupAnimationComponent() {
        Animation static_pos = AnimationBuilder.buildAnimation(this.tombImg);
        new AnimationComponent(this, static_pos);
    }

    @Override
    public void update(Set<Entity> entities, int level) {
        this.ghost.update();
        if (this.rewardOrPunishment && !this.isRewarded) {
            this.rewardOrPunishment = false;
            this.isRewarded = true;
            int rndRewardOrPunishment = new Random().nextInt(2);
            if (rndRewardOrPunishment == 0) {
                System.out.println("ITEM");
                ItemDataGenerator itm = new ItemDataGenerator();
                int rnd = new Random().nextInt(itm.getAllItems().size());
                ItemData item = itm.getItem(rnd);
                entities.add(WorldItemBuilder.buildWorldItem(item, this.position.getPosition()));
            } else {
                System.out.println("SKELETT");
                Skeleton skeleton = new Skeleton(level);
                skeleton.setToTomb(this.position);
                entities.add(skeleton);
            }
        }
    }

    private void setupCollision() {
        new HitboxComponent(this, (you, other, direction) -> setGhostPosition());
    }

    private void setGhostPosition() {
        this.ghost.setToTomb(this.position);
        this.rewardOrPunishment = true;
    }

    public Ghost getGhost() {
        return ghost;
    }

    public void setGhost(Ghost ghost) {
        this.ghost = ghost;
    }
}
