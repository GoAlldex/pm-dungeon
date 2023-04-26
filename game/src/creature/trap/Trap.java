package creature.trap;

import ecs.components.PositionComponent;
import ecs.entities.Entity;
import graphic.Animation;

/**
 *
 * @author Ayaz Khudhur
 * @version 1.0
 */
public abstract class Trap extends Entity{
    private PositionComponent position; //Component position
    private boolean visibility; //trap visibility
    private Animation worldAnimation; //Trap animation
    private boolean placedSwitch; //trap switch position

    /**
     * @param entity set entity position
     */
    public void position(Entity entity){
        position = new PositionComponent(entity);
    }

    /**
     * @return get entity position
     */
    public PositionComponent position(){
        return position;
    }

    /**
     * @param visibility set trap visibility
     */
    public void visibility(boolean visibility){
        this.visibility = visibility;
    }

    /**
     * @return get trap visibility
     */
    public boolean visibility(){
        return visibility;
    }

    /**
     * @return get trap place
     */
    public boolean placedSwitch(){
        return placedSwitch;
    }

    /**
     * @param placedSwitch set trap place
     */
    public void placedSwitch(boolean placedSwitch){
        this.placedSwitch = placedSwitch;
    }

    /**
     * @param animation set world animation
     */
    public void animation(Animation animation){
        this.worldAnimation = animation;
    }

    /**
     * @return get world animation
     */
    public Animation animation(){
        return worldAnimation;
    }
}
