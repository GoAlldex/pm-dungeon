package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import graphic.Animation;
import java.util.List;
import level.elements.tile.FloorTile;

public class SpikesTrap extends TrapGenerator {
    private static final String illusionPath = "dungeon/default/floor/floor_1.png";
    private static final String visibilityPath = "dungeon/default/floor/spikes.png";

    public SpikesTrap(List<FloorTile> floorTiles) {
        super();
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(false);
        placedSwitch(false);
        animation();
        setupHitboxComponent();
    }

    @Override
    public Animation animation() {
        return visibility() ? showTrap() : hiddenTrap();
    }

    @Override
    public Animation showTrap() {
        if (visibility()) {
            Animation animation = AnimationBuilder.buildAnimation(visibilityPath);
            new AnimationComponent(this, animation);
            return animation;
        } else {
            return hiddenTrap();
        }
    }

    @Override
    public Animation hiddenTrap() {
        if (!visibility()) {
            Animation animation = AnimationBuilder.buildAnimation(illusionPath);
            new AnimationComponent(this, animation);
            return animation;
        } else {
            return showTrap();
        }
    }
}
