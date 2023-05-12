package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.tile.FloorTile;

import java.util.List;

public class TeleportTrap extends TrapGenerator{
    private static final String teleportPath = FLOORPATH + "teleport.png";
    private static final String illusionPath = FLOORPATH + "floor_1.png";

    private Entity entity;

    public TeleportTrap(List<FloorTile> floorTiles, Entity entity){
        super();
        this.entity = entity;
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(false);
        placedSwitch(false);
        animation();
        setupHitboxComponent();
    }

    @Override
    public Animation animation() {
        return visibility() ?  showTrap() : hiddenTrap();
    }

    @Override
    public Animation showTrap(){
        if (visibility()) {
            Animation animation = AnimationBuilder.buildAnimation(teleportPath);
            new AnimationComponent(this, animation);
            return animation;
        }else {
            return hiddenTrap();
        }
    }

    @Override
    public Animation hiddenTrap(){
        if (!visibility()) {
            Animation animation = AnimationBuilder.buildAnimation(illusionPath);
            new AnimationComponent(this, animation);
            return animation;
        }else {
            return showTrap();
        }
    }

    @Override
    public void setupHitboxComponent() {
        super.setupHitboxComponent();
    }
}
