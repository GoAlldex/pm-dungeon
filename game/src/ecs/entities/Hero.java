package ecs.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.*;
import ecs.items.ItemDataGenerator;
import graphic.Animation;
import graphic.hud.PauseMenu;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity {

    private final int fireballCoolDown = 5;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";
    private Skill firstSkill;

    private InventoryComponent inventory;
    private boolean inventoryOpen = false;
    int cd = 30;

    /** Entity with Components */
    public Hero() {
        super();
        new PositionComponent(this);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        PlayableComponent pc = new PlayableComponent(this);
        setupFireballSkill();
        pc.setSkillSlot1(firstSkill);
        setDefaultItems();
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, xSpeed, ySpeed, moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        new AnimationComponent(this, idleLeft, idleRight);
    }

    private void setupFireballSkill() {
        firstSkill =
                new Skill(
                        new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> System.out.println("heroCollisionEnter"),
                (you, other, direction) -> System.out.println("heroCollisionLeave"));
    }

    private void setDefaultItems() {
        this.inventory = new InventoryComponent(this, 9);
        ItemDataGenerator itm = new ItemDataGenerator();
        this.inventory.addItem(itm.getItem(0));
        this.inventory.addItem(itm.getItem(1));
    }
    @Override
    public void update(PauseMenu<Actor> pauseMenu) {
        if(cd != 30) {
            cd++;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.B) && !inventoryOpen && cd == 30) {
            cd = 0;
            this.inventoryOpen = true;
            pauseMenu.showMenu();
            this.inventory.printAllItems();
        } else if(Gdx.input.isKeyPressed(Input.Keys.B) && inventoryOpen && cd == 30) {
            cd = 0;
            this.inventoryOpen = false;
            pauseMenu.hideMenu();
        }
    }

}
