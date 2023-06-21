package ecs.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.idle.StaticRadiusWalk;
import ecs.items.ItemDataGenerator;
import graphic.Animation;
import graphic.hud.Dialog;
import graphic.hud.inventory.EntityGraphicInventory;
import graphic.hud.inventory.HeroGraphicInventory;
import starter.Game;

import java.util.Random;
import java.util.logging.Logger;

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
    private boolean dialogueIsOpen = false;
    private boolean collision = false;
    private Dialog dialog;
    private int delay = 0;
    private EntityGraphicInventory graphicInventory;
    private boolean isOpen = false;
    private static final Logger log = Logger.getLogger(Ghost.class.getName());

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
        setupHitboxComponent();
        setDefaultItems();
    }

    private void setDefaultItems() {
        this.inventory = new InventoryComponent(this, 10);
        ItemDataGenerator itm = new ItemDataGenerator();
        this.inventory.addItem(itm.getItem(0));
        this.inventory.addItem(itm.getItem(1));
        this.inventory.addItem(itm.getItem(2));
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
        //dialogue();
        animation();
        graphicInventory();
    }

    private void graphicInventory() {
        if(this.collision) {
            if(this.delay == 0) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) && this.isOpen) {
                    if(Game.getPause()) {
                        Game.togglePause();
                    }
                    this.delay = 30;
                    log.info("Inventar wird geschlossen");
                    this.isOpen = false;
                    this.graphicInventory.closeInventory();
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !this.isOpen) {
                    if(!Game.getPause()) {
                        Game.togglePause();
                    }
                    this.delay = 30;
                    log.info("Inventar wird geöffnet");
                    this.isOpen = true;
                    this.graphicInventory = new EntityGraphicInventory(this);
                    this.graphicInventory.openInventory();
                }
            } else {
                this.delay--;
            }
        }
    }

    private void animation() {
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

    private void dialogue() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.E) && !dialogueIsOpen && collision) {
            dialogueIsOpen = true;
            Game.togglePause();
            dialog = new Dialog();
            dialog.createPanel();
            Game.controller.add(dialog);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.E) && dialogueIsOpen) {
            dialogueIsOpen = false;
            Game.controller.remove(dialog);
            Game.togglePause();
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && dialogueIsOpen) {
            dialog.setText();
        }
    }

    public boolean getDialog() {
        return this.dialogueIsOpen;
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> {
                collision = true;
            },
            (you, other, direction) -> {
                collision = false;
            });
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

    /**
     * <b><span style="color: rgba(3,71,134,1);">Setze den Geist zum Grabstein</span></b><br>
     * Setzt den Geist zum Grabstein
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_2
     * @since 08.05.2023
     */
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
