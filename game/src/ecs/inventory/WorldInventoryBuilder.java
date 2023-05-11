package ecs.inventory;

import ecs.components.*;
import ecs.entities.Entity;
import graphic.Animation;
import tools.Point;

import java.util.List;

public class WorldInventoryBuilder {

    private static List<String> invTex;
    /**
     * Creates an Entity which then can be added to the game
     *
     * @param inv the Data which should be given to the world Inventory
     * @return the newly created Entity
     */
    public static Entity buildWorldInventory(InventoryComponent inv, Point point) {
        Entity inventory = new Entity();
        new PositionComponent(inventory, point);
        for(int i = 0; i < inv.filledSlots(); i++) {
            invTex.add(inv.getItem(i).getInventoryTexture().getNextAnimationTexturePath());
        }
        for(int i = 0; i < inv.emptySlots(); i++) {
            invTex.add(inv.getTexture());
        }
        new AnimationComponent(inventory, new Animation(invTex, 1));
        return inventory;
    }
}
