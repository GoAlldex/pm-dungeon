package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.ILevel;
import level.elements.tile.FloorTile;
import level.elements.tile.Tile;
import level.elements.tile.WallTile;
import level.generator.IGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.Coordinate;
import tools.Point;

import java.util.List;
import java.util.Random;

public class TeleportTrap extends TrapGenerator{
    private static final String teleportPath = "dungeon/default/floor/teleport.png";
    private static final String illusionPath = "dungeon/default/floor/floor_1.png";

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
        return visibility() ?  showTrap() : hiddenTrap();
    }

    @Override
    public Animation showTrap(){
        if (visibility()) {
            Animation animation = AnimationBuilder.buildAnimation(teleportPath);
            new AnimationComponent(this, animation);
            teleport();
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
    private void teleport(){
        IGenerator generator = new RandomWalkGenerator();
        //System.out.println("Random walk generator: " + generator.getLevel().printLevel());
    }
}
