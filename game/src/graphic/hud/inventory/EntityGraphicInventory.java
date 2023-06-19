package graphic.hud.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import ecs.components.InventoryComponent;
import ecs.entities.Entity;
import ecs.entities.Hero;
import ecs.items.ItemData;
import graphic.hud.*;
import graphic.hud.inventory.GraphicInventory;
import starter.Game;
import tools.Constants;
import tools.Point;
import java.util.logging.Logger;
/**
 * <b><span style="color: rgba(3,71,134,1);">Unser Grafisches Inventar.</span></b><br>
 * Hier werden die wichtigesten bestandteile unseres Grafischen Inventars initialisiert.<br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_5
 * @since 17.06.2023
 */
public class EntityGraphicInventory <T extends Actor> extends ScreenController<T> {

    private Entity entity;
    private boolean isOpen = false;
    private String emptyIcon = "inventory/ui_bag_empty.png";
    private ScreenImage[] inventoryImages;
    private ScreenImage background;
    private String bag_bg = "inventory/bag_bg.png";

    private ScreenText legend;
    private int delay = 0;
    private float scale = 3f;
    private int inventorySize = 35;
    private static final Logger log = Logger.getLogger(GraphicInventory.class.getName());

    /**
     * <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     * Initialisiert das Grafische Inventar.
     *
     ** @param entity Übergibt die Entity, die das Inventar besitzen soll
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public EntityGraphicInventory(Entity entity) {
        this(entity, new SpriteBatch());
    }
    public EntityGraphicInventory(Entity entity, SpriteBatch batch) {
        super(batch);
        this.entity = entity;
        closeInventory();
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Inventar öffnen</span></b><br>
     * Diese Methode zeigt das Inventar grafisch an, es erzeugt die einzelnen Inventar Kacheln und versieht sie mit den click Methoden für das Item
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public void openInventory() {
        closeInventory();
        this.background = new ScreenImage(this.bag_bg, new Point(0, 0));
        this.background.setScale(this.scale);
        this.background.setPosition((Constants.WINDOW_WIDTH/40f+this.scale), Constants.WINDOW_HEIGHT/7.5f);
        add((T) this.background);
        this.legend = new ScreenText("Links klick Item verschieben", new Point(0,0),1f, new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.WHITE).build());
        this.legend.setPosition((Constants.WINDOW_WIDTH/9f), Constants.WINDOW_HEIGHT/6.5f);
        add((T) this.legend);
        InventoryComponent inventory = this.entity.getInventory();
        this.inventoryImages = new ScreenImage[inventorySize];
        float nHeight = 0;
        int count = 0;
        for(int i = 0; i < inventorySize; i++) {
            if((i%5) == 0) {
                nHeight += this.scale;
                count = 0;
            }
            if(inventory.getItems().size() < (i+1)) {
                this.inventoryImages[i] = new ScreenImage(this.emptyIcon, new Point(0, 0));
                this.inventoryImages[i].setScale(this.scale);
                this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/15f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
            } else {
                this.inventoryImages[i] = new ScreenImage(inventory.getItem(i).getInventoryTexture().getNextAnimationTexturePath(), new Point(0, 0));
                this.inventoryImages[i].setScale(this.scale);
                this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH / 15f + (count * 16 * this.scale)), ((Constants.WINDOW_HEIGHT / 1.1f) - (16 * nHeight)));
                int finalI = i;
                this.inventoryImages[i].addListener(
                    new TextButtonListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            checkItem(finalI);
                        }
                    });
            }
            add((T) this.inventoryImages[i]);
            count++;
        }
        this.forEach((Actor s) -> s.setVisible(true));
        Game.controller.add(this);
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Inventar schließen</span></b><br>
     * Diese Methode schließt das Inventar und entfernt alle sichtbaren Elemente
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public void closeInventory() {
        this.isOpen = false;
        this.forEach((Actor s) -> s.setVisible(false));
        this.forEach((Actor s) -> s.remove());
        if(this.background != null) {
            remove((T) this.background);
        }
        if(this.legend != null) {
            remove((T) this.legend);
        }
        if(this.inventoryImages != null) {
            if(this.inventoryImages.length != 0) {
                for (int i = 0; i < this.inventoryImages.length; i++) {
                    remove((T) this.inventoryImages[i]);
                }
            }
        }
        Game.controller.remove(this);
    }

    private void checkItem(int index) {
        moveItem(index);
        openInventory();
    }

    private void moveItem(int index) {
        Hero hero =  Game.getHeroEntity();
        if(hero.getInventory().emptySlots() != 0) {
            ItemData item = this.entity.getInventory().getItem(index);
            this.entity.getInventory().removeItem(item);
            hero.getInventory().addItem(item);
        }
        log.info("Item in das Helden Inventar verschoben");
    }

}
