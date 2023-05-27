package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.idle.StaticRadiusWalk;
import graphic.Animation;
import java.util.Random;

/**
 * <b><span style="color: rgba(3,71,134,1);">Unsere NPC-Klasse "Geist".</span></b><br>
 * Hier werden die wichtigesten bestandteile unseres NPC "Geist" initialisiert.<br>
 * <br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_2
 * @since 08.05.2023
 */
public class Ghost extends NPC {

    private boolean visible = true;
    private boolean followHero = false;
    private int invisibleFrames = 150;
    private int frameCounter = 0;
    private AnimationComponent ani;
    private String empty = "npc/ghost/empty/";
    private boolean tomb = false;

    /**
     * <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     * Initialisiert ein neuen NPC "Geist".
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_2
     * @since 08.05.2023
     */
    public Ghost() {
        super();
        this.position = new PositionComponent(this);
        this.speed[0] = 0.25f;
        this.speed[1] = 0.25f;
        this.pathToIdleLeft = "npc/ghost/idleLeft";
        this.pathToIdleRight = "npc/ghost/idleRight";
        this.pathToRunLeft = "npc/ghost/runLeft";
        this.pathToRunRight = "npc/ghost/runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        this.ai = new AIComponent(this);
        monsterMoveStrategy();
        this.ai.execute();
    }

    private void monsterMoveStrategy() {
        Random rnd = new Random();
        int strategy = rnd.nextInt(4);
        int radius = rnd.nextInt(8) + 2;
        int checkPoints = rnd.nextInt(3) + 2;
        int pauseTime = rnd.nextInt(5) + 1;
        switch (strategy) {
            case 0:
                this.ai.setIdleAI(
                        new PatrouilleWalk(
                                radius, checkPoints, pauseTime, PatrouilleWalk.MODE.LOOP));
                break;
            case 1:
                this.ai.setIdleAI(new RadiusWalk(radius, pauseTime));
                break;
            case 2:
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
        this.ani = new AnimationComponent(this, idleLeft, idleRight);
    }

    @Override
    public void update() {
        if (!isTomb()) {
            this.frameCounter++;
            if (this.frameCounter == this.invisibleFrames) {
                setVisible(false);
                this.ani =
                        new AnimationComponent(
                                this,
                                AnimationBuilder.buildAnimation(this.empty),
                                AnimationBuilder.buildAnimation(this.empty));
            }
            if (this.frameCounter == (this.invisibleFrames * 2)) {
                setVisible(true);
                setupAnimationComponent();
                this.frameCounter = 0;
            }
        } else {
            if (!isVisible()) {
                setVisible(true);
                setupAnimationComponent();
                this.frameCounter = 0;
            }
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isTomb() {
        return tomb;
    }

    public void setTomb(boolean tomb) {
        this.tomb = tomb;
    }

    public void setToTomb(PositionComponent position) {
        if (!this.tomb) {
            this.speed[0] = 0;
            this.speed[1] = 0;
            setupVelocityComponent();
            this.position = new PositionComponent(this, position.getPosition());
            this.tomb = true;
        }
    }
}
