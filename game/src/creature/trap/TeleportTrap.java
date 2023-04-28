package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import graphic.Animation;
import level.elements.tile.FloorTile;
import java.util.List;

public class TeleportTrap extends TrapGenerator{
    private static final String teleportPath = FLOORPATH + "teleport.png";

    public TeleportTrap(List<FloorTile> floorTiles){
        super();
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(true);
        placedSwitch(false);
        animation();
        setupHitboxComponent();
    }

    @Override
    public Animation animation() {
        return visibility() ? showTrap() : AnimationBuilder.buildAnimation(ILLUSIONPATH);
    }

    @Override
    public Animation showTrap(){
        Animation animation = new Animation(AnimationBuilder.buildAnimation(teleportPath).getAnimationFrames(), 1);
        AnimationComponent component = new AnimationComponent(this, animation);
        return animation;
    }

    @Override
    public void setupHitboxComponent() {
        new HitboxComponent(
          this,
            (a, b, direction) -> teleport(),
            (a, b, direction) -> System.out.println("Leave " + TeleportTrap.class)
        );
    }

    private void teleport() {
        visibility(true);
        showTrap();
        System.out.println("Teleport...");
    }
}
