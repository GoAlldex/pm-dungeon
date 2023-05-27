package creature.trap;

import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.List;
import java.util.Random;
import level.elements.tile.FloorTile;
import level.tools.Coordinate;
import tools.Point;

/**
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_1
 */
public abstract class TrapGenerator extends Trap {
    // Get floor list
    private List<FloorTile> floorTiles;
    // default position
    private PositionComponent position;
    // trap damage
    private final float dmg = 0.1f;

    public static final String FLOORPATH = "dungeon/default/floor/";

    /** Generate a random position */
    public void generatePosition() {
        Coordinate[] coordinates = new Coordinate[getFloorTiles().size()];
        for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = getFloorTiles().get(i).getCoordinate();
        }

        int index = new Random().nextInt(coordinates.length);
        position(this);
        position = new PositionComponent(this);
        position.setPosition(new Point(coordinates[index].toPoint()));
    }

    /**
     * set position of trap
     *
     * @param entity current class is the entity
     */
    @Override
    public void position(Entity entity) {
        super.position(entity);
        position = new PositionComponent(entity);
    }

    /**
     * @param visibility should the trap indicate or not
     */
    @Override
    public void visibility(boolean visibility) {
        super.visibility(visibility);
        showTrap();
        hiddenTrap();
    }

    /**
     * @return return the trap animation
     */
    public Animation showTrap() {
        return null;
    }

    /**
     * @return a new Animation for Illusion trap
     */
    public Animation hiddenTrap() {
        return null;
    }

    /** Setting the hitboxcomponent */
    public void setupHitboxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> visibility(true),
                (you, other, direction) ->
                        System.out.println("Leave..." + getClass().getSimpleName()));
    }

    @Override
    public Animation animation() {
        position(); // GET RANDOM POSITION
        return super.animation();
    }

    /**
     * @return get floorTiles
     */
    public List<FloorTile> getFloorTiles() {
        return floorTiles;
    }

    /**
     * @param floorTiles set FloorTiles
     */
    public void setFloorTiles(List<FloorTile> floorTiles) {
        this.floorTiles = floorTiles;
    }

    public float getDmg() {
        return dmg;
    }
}
