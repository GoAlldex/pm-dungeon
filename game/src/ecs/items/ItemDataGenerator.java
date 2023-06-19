package ecs.items;

import graphic.Animation;
import java.util.List;
import java.util.Random;

/** Generator which creates a random ItemData based on the Templates prepared. */
public class ItemDataGenerator {
    // private static final List<String> missingTexture = List.of("animation/missingTexture.png");
    private static final List<String> potion_hp_world =
            List.of(
                    "animation/Items/Potions/Big_Red_Potion/drop_animation_1.png",
                    "animation/Items/Potions/Big_Red_Potion/drop_animation_2.png",
                    "animation/Items/Potions/Big_Red_Potion/drop_animation_3.png",
                    "animation/Items/Potions/Big_Red_Potion/drop_animation_4.png");
    private static final List<String> potion_hp_inventory =
            List.of("animation/Items/Potions/Big_Red_Potion/inventory_icon.png");
    private static final List<String> weapon1_world =
            List.of("animation/Items/Weapons/Weapons_melee/Weapon_Sword_Blue_1/show.png");
    private static final List<String> weapon1_inventory =
            List.of("animation/Items/Weapons/Weapons_melee/Weapon_Sword_Blue_1/inventory_icon.png");
    private static final List<String> potion_mana_world =
        List.of(
            "animation/Items/Potions/Big_Blue_Potion/drop_animation_1.png",
            "animation/Items/Potions/Big_Blue_Potion/drop_animation_2.png",
            "animation/Items/Potions/Big_Blue_Potion/drop_animation_3.png",
            "animation/Items/Potions/Big_Blue_Potion/drop_animation_4.png");
    private static final List<String> potion_mana_inventory =
        List.of("animation/Items/Potions/Big_Blue_Potion/inventory_icon.png");

    private List<ItemData> templates =
            List.of(
                    new ItemData(
                            ItemType.HP,
                            new Animation(potion_hp_world, 1),
                            new Animation(potion_hp_inventory, 1),
                            "HP Trank",
                            "Füllt die HP um 10% auf."),
                    new ItemData(
                            ItemType.WEAPON,
                            new Animation(weapon1_world, 1),
                            new Animation(weapon1_inventory, 1),
                            "Schwert",
                            "Fügt Schaden zu."),
                    new ItemData(
                            ItemType.MANA,
                            new Animation(potion_mana_world, 1),
                            new Animation(potion_mana_inventory, 1),
                            "Mana Trank",
                            "Füllt das Mana um 10% auf."));
    private Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Ein Item</span></b><br>
     * Rückgabe ein Item
     *
     * @return ItemData Ein Item
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_1
     * @since 26.04.2023
     */
    public ItemData getItem(int index) {
        if (index >= this.templates.size()) {
            return null;
        } else {
            return this.templates.get(index);
        }
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Alle Items</span></b><br>
     * Rückgabe alle Items
     *
     * @return List<ItemData> Alle Items
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_1
     * @since 26.04.2023
     */
    public List<ItemData> getAllItems() {
        return this.templates;
    }
}
