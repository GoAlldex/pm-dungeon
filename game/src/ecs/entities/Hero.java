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

import java.util.ArrayList;
import java.util.Set;

/**
 * The Hero is the player character. It's entity in the ECS. This class helps to setup the hero with
 * all its components and attributes .
 */
public class Hero extends Entity {

    private final int fireballCoolDown = 5;
    private final int arrowCoolDown = 1;
    private final int boomerangCoolDown = 2;
    private final float xSpeed = 0.3f;
    private final float ySpeed = 0.3f;

    private final String pathToIdleLeft = "knight/idleLeft";
    private final String pathToIdleRight = "knight/idleRight";
    private final String pathToRunLeft = "knight/runLeft";
    private final String pathToRunRight = "knight/runRight";
    private Skill firstSkill = new Skill(
            new FireballSkill(SkillTools::getCursorPositionAsPoint), fireballCoolDown);

    private Skill secondSkill = new Skill(
        new ArrowSkill(SkillTools::getCursorPositionAsPoint), arrowCoolDown);

    private Skill thirdSkill = new Skill(
        new BoomerangSkill(SkillTools::getCursorPositionAsPoint), boomerangCoolDown);


    private InventoryComponent inventory;
    int cd = 30;
    private HealthComponent hp;
    private IOnDeathFunction death;
    private PositionComponent position;

    /** Entity with Components */
    public Hero() {
        super();
        this.position = new PositionComponent(this);
        onDeath();
        this.hp = new HealthComponent(this, 100, this.death, null, null);
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        PlayableComponent pc = new PlayableComponent(this);
        setupFireballSkill();
        setupArrowSkill();
        pc.setSkillSlot1(firstSkill);
        pc.setSkillSlot2(secondSkill);
        pc.setSkillSlot3(thirdSkill);
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

    private void setupArrowSkill(){
        secondSkill =
            new Skill (
                        new ArrowSkill(SkillTools::getCursorPositionAsPoint), arrowCoolDown);
    }

    private void setupBoomerangSkill(){
        thirdSkill =
            new Skill (
                       new BoomerangSkill(SkillTools::getCursorPositionAsPoint), boomerangCoolDown);
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
    public void update() {

    }
    @Override
    public InventoryComponent getInventory() {
        return this.inventory;
    }

    public void onDeath() {
        this.death = new IOnDeathFunction() {
            @Override
            public void onDeath(Entity entity) {

            }
        };
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Position</span></b><br>
     Position des Helden im Dungeon Level.
     @return PositionComponent gibt die Position des Helden zurück
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_2
     @since 08.05.2023
     */
    public PositionComponent getPosition() {
        return position;
    }

}
