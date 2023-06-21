package graphic.hud.inventory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import ecs.components.InventoryComponent;
import ecs.entities.Hero;
import ecs.items.ItemData;
import ecs.items.ItemType;
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
public class HeroGraphicInventory<T extends Actor> extends ScreenController<T> {

    private Hero entity;
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
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public HeroGraphicInventory(Hero hero) {
        this(hero, new SpriteBatch());
    }
    public HeroGraphicInventory(Hero hero, SpriteBatch batch) {
        super(batch);
        this.entity = hero;
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
        this.isOpen = true;
        this.background = new ScreenImage(this.bag_bg, new Point(0, 0));
        this.background.setScale(this.scale);
        this.background.setPosition((Constants.WINDOW_WIDTH/1.9f+this.scale), Constants.WINDOW_HEIGHT/7.5f);
        add((T) this.background);
        this.legend = new ScreenText("Links klick Item benutzen", new Point(0,0),1f, new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.WHITE).build());
        this.legend.setPosition((Constants.WINDOW_WIDTH/1.6f), Constants.WINDOW_HEIGHT/6.5f);
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
                this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/1.75f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
            } else {
                this.inventoryImages[i] = new ScreenImage(inventory.getItem(i).getInventoryTexture().getNextAnimationTexturePath(), new Point(0, 0));
                this.inventoryImages[i].setScale(this.scale);
                this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/1.75f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
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
        this.stage.clear();
        this.stage.dispose();
    }

    private void checkItem(int index) {
        ItemData item = this.entity.getInventory().getItem(index);
        if(item.getItemType() != ItemType.WEAPON) {
            useItem(index);
        } else {
            weaponItem(index);
        }
        openInventory();
    }

    private void useItem(int index) {
        ItemData item = this.entity.getInventory().getItem(index);
        if(item.getItemType() == ItemType.HP) {
            if(this.entity.getHP().getCurrentHealthpoints() != this.entity.getHP().getMaximalHealthpoints()) {
                int maxHp = this.entity.getHP().getMaximalHealthpoints();
                int heal = Math.round(maxHp * 0.1f);
                if (maxHp >= (heal + this.entity.getHP().getCurrentHealthpoints())) {
                    heal = heal + this.entity.getHP().getCurrentHealthpoints();
                    this.entity.getHP().setCurrentHealthpoints(heal);
                    log.info("Held geheilt um: "+heal+" HP");
                } else {
                    this.entity.getHP().setCurrentHealthpoints(maxHp);
                }
                this.entity.getInventory().removeItem(item);
            } else {
                log.info("Held hat bereits volle HP");
            }
        }
        if(item.getItemType() == ItemType.MANA) {
            if(this.entity.getMc().getCurrentManaPoint() != this.entity.getMc().getMaxManaPoint()) {
                int maxMana = this.entity.getMc().getMaxManaPoint();
                int mana = Math.round(maxMana * 0.1f);
                if (maxMana >= (mana + this.entity.getMc().getCurrentManaPoint())) {
                    mana = mana + this.entity.getMc().getCurrentManaPoint();
                    this.entity.getMc().setCurrentManaPoint(mana);
                    log.info("Held Mana aufgefüllt um: "+mana+" Punkte");
                } else {
                    this.entity.getHP().setCurrentHealthpoints(maxMana);
                }
                this.entity.getInventory().removeItem(item);
            } else {
                log.info("Held hat bereits volles Mana");
            }
        }
    }

    private void weaponItem(int index) {
        ItemData item = this.entity.getInventory().getItem(index);
        if(this.entity.getWeapon() == null) {
            this.entity.setWeapon(item);
            log.info("Waffe ausgerüstet");
        } else {
            this.entity.getInventory().addItem(this.entity.getWeapon());
            this.entity.setWeapon(item);
            log.info("Waffe getauscht");
        }
        this.entity.getInventory().removeItem(item);
    }

}
