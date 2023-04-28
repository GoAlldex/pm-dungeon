package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.tile.WallTile;
import level.tools.Coordinate;
import tools.Point;

import java.util.List;
import java.util.Random;

/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_1
 */
public class Switch extends Entity{
    private PositionComponent position;
    private Animation worldAnimation;
    private Trap trap;

    private List<WallTile> wallTiles;

    public Switch(List<WallTile> wallTiles){
        this.wallTiles = wallTiles;
    }

    public void position(Entity entity){
        generatePosition();
    }

    private void generatePosition(){
        Coordinate[] coordinates = new Coordinate[wallTiles.size()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = wallTiles.get(i).getCoordinate();
        }
        int index = new Random().nextInt(coordinates.length);
        position(this);
        position = new PositionComponent(this);
        position.setPosition(new Point(coordinates[index].toPoint()));
    }

    public void animation(){
        //Beispiel
        worldAnimation = AnimationBuilder.buildAnimation(TrapGenerator.FLOORPATH + "monsterTrap.png");
        new AnimationComponent(this, worldAnimation);
    }

    public void trap(){
        new HitboxComponent(
            this,
            (you, other, direction) -> trap.setTrigger(true),
            (you, other, direction) -> System.out.println("Leave..." + getClass().getSimpleName())
        );
    }

}
