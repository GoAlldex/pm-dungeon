package ecs.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import ecs.entities.Entity;
import ecs.items.ItemData;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import graphic.Animation;
import logging.CustomLogLevel;
import tools.Point;

/** Allows an Entity to carry Items */
public class InventoryComponent extends Component {

    private List<ItemData> inventory;
    private int maxSize;
    private final Logger inventoryLogger = Logger.getLogger(this.getClass().getName());
    private static final String inventoryTex = "inventory/ui_bag_empty.png";

    /**
     * creates a new InventoryComponent
     *
     * @param entity the Entity where this Component should be added to
     * @param maxSize the maximal size of the inventory
     */
    public InventoryComponent(Entity entity, int maxSize) {
        super(entity);
        inventory = new ArrayList<>(maxSize);
        this.maxSize = maxSize;
    }

    /**
     * Adding an Element to the Inventory does not allow adding more items than the size of the
     * Inventory.
     *
     * @param itemData the item which should be added
     * @return true if the item was added, otherwise false
     */
    public boolean addItem(ItemData itemData) {
        if (inventory.size() >= maxSize) return false;
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Item '"
                        + this.getClass().getSimpleName()
                        + "' was added to the inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.add(itemData);
    }

    /**
     * removes the given Item from the inventory
     *
     * @param itemData the item which should be removed
     * @return true if the element was removed, otherwise false
     */
    public boolean removeItem(ItemData itemData) {
        inventoryLogger.log(
                CustomLogLevel.DEBUG,
                "Removing item '"
                        + this.getClass().getSimpleName()
                        + "' from inventory of entity '"
                        + entity.getClass().getSimpleName()
                        + "'.");
        return inventory.remove(itemData);
    }

    /**
     * @return the number of slots already filled with items
     */
    public int filledSlots() {
        return inventory.size();
    }

    /**
     * @return the number of slots still empty
     */
    public int emptySlots() {
        return maxSize - inventory.size();
    }

    /**
     * @return the size of the inventory
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * @return a copy of the inventory
     */
    public List<ItemData> getItems() {
        return new ArrayList<>(inventory);
    }

    public ItemData getItem(int item) {
        return inventory.get(item);
    }

    public void printAllItems() {
        int s = 0;
        System.out.println("Inventar");
        System.out.println("_______________________________");
        for(ItemData i : getItems()) {
            System.out.println("Item Slot "+s+": "+i.getItemName());
            s++;
        }
        System.out.println("_______________________________");
    }

    public String getTexture() {
        return this.inventoryTex;
    }

}
