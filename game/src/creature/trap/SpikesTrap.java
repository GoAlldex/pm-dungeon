package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import graphic.Animation;
import level.elements.tile.FloorTile;

import java.util.List;

public class SpikesTrap extends TrapGenerator{
    private static final String visibilityPath ="dungeon/default/floor/spikes.png";
    private int levelCounter;

    public SpikesTrap(List<FloorTile> floorTiles, int levelCounter){
        super();
        this.levelCounter = levelCounter;
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(true);
        placedSwitch(false);
        new AnimationComponent(this, animation());
        setupHitboxComponent();
    }

    @Override
    public Animation animation(){
        return visibility() ?  showTrap() : AnimationBuilder.buildAnimation(ILLUSIONPATH);
    }

    @Override
    public Animation showTrap(){
        Animation animation = new Animation(AnimationBuilder.buildAnimation(visibilityPath).getAnimationFrames(), 1);
        AnimationComponent component = new AnimationComponent(this, animation);
        return animation;
    }

    @Override
    public void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (a,b,direction) -> spikes(),
            (a,b,direction) -> System.out.println("Leave..."+SpikesTrap.class)
        );
    }

    private void spikes() {
        visibility(true);
        showTrap();
        System.out.println("Damage: " + getDmg() * 0.2f * levelCounter);
    }
}
