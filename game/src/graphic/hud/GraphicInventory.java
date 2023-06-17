package graphic.hud;

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
import ecs.items.ItemType;
import starter.Game;
import tools.Constants;
import tools.Point;
import java.util.logging.Logger;
/**
 * <b><span style="color: rgba(3,71,134,1);">Unser Grafisches Inventar.</span></b><br>
 * Hier werden die wichtigesten bestandteile unseres Grafischen Inventars initialisiert.<br>
 * <br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_5
 * @since 17.06.2023
 */
public class GraphicInventory <T extends Actor> extends ScreenController<T> {

    private Entity entity;
    private boolean isHero;
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
    public GraphicInventory(Entity entity, boolean isHero) {
        this(entity, isHero, new SpriteBatch());
    }
    public GraphicInventory(Entity entity, boolean isHero, SpriteBatch batch) {
        super(batch);
        this.entity = entity;
        this.isHero = isHero;
        Game.controller.add(this);
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Inventar anzeigen</span></b><br>
     * Öffnet und schließt das Inventar, der Held wird in diese Methode gesondert behandelt
     * Mit I öffnet der Held sein Inventar (rechts)
     * Mit E wird das Entity und Helden Inventar geöffnet
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public void renderInventory() {
        if(this.delay == 0) {
            if(this.isHero) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.I) && this.isOpen) {
                    if(Game.getPause()) {
                        Game.togglePause();
                    }
                    this.delay = 30;
                    log.info("Inventar wird geschlossen");
                    this.isOpen = false;
                    closeInventory();
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.I) && !this.isOpen) {
                    if(!Game.getPause()) {
                        Game.togglePause();
                    }
                    this.delay = 30;
                    log.info("Inventar wird geöffnet");
                    this.isOpen = true;
                    openInventory();
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.E) && this.isOpen) {
                    if(Game.getPause()) {
                        Game.togglePause();
                    }
                    Game.getHeroEntity().getGraphicInventory().closeInventory();
                    this.delay = 30;
                    log.info("Inventar wird geschlossen");
                    this.isOpen = false;
                    closeInventory();
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !this.isOpen) {
                    if(!Game.getPause()) {
                        Game.togglePause();
                    }
                    Game.getHeroEntity().getGraphicInventory().openInventory();
                    this.delay = 30;
                    log.info("Inventar wird geöffnet");
                    this.isOpen = true;
                    openInventory();
                }
            }
        } else {
            this.delay--;
        }
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Inventar öffnen/schließen</span></b><br>
     * Diese Methode ist für den HUD Inventar Button gedacht, es öffnet oder schließt das Helden Inventar
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public void hudInventory() {
        if(this.isOpen) {
            log.info("Inventar wird geschlossen");
            this.isOpen = false;
            closeInventory();
        } else {
            log.info("Inventar wird geöffnet");
            this.isOpen = true;
            openInventory();
        }
    }

    private void openInventory() {
        closeInventory();
        if(this.isHero) {
            this.background = new ScreenImage(this.bag_bg, new Point(0, 0));
            this.background.setScale(this.scale);
            this.background.setPosition((Constants.WINDOW_WIDTH/1.9f+this.scale), Constants.WINDOW_HEIGHT/7.5f);
            add((T) this.background);
            this.legend = new ScreenText("Links klick Item benutzen", new Point(0,0),1f, new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.WHITE).build());
            this.legend.setPosition((Constants.WINDOW_WIDTH/1.6f), Constants.WINDOW_HEIGHT/6.5f);
            add((T) this.legend);
        } else {
            this.background = new ScreenImage(this.bag_bg, new Point(0, 0));
            this.background.setScale(this.scale);
            this.background.setPosition((Constants.WINDOW_WIDTH/40f+this.scale), Constants.WINDOW_HEIGHT/7.5f);
            add((T) this.background);
            this.legend = new ScreenText("Links klick Item benutzen", new Point(0,0),1f, new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.WHITE).build());
            this.legend.setPosition((Constants.WINDOW_WIDTH/8f), Constants.WINDOW_HEIGHT/6.5f);
            add((T) this.legend);
        }
        InventoryComponent inventory = this.entity.getInventory();
        this.inventoryImages = new ScreenImage[inventorySize];
        float nHeight = 0;
        int count = 0;
        for(int i = 0; i < inventorySize; i++) {
            if((i%5) == 0) {
                nHeight += this.scale;
                count = 0;
            }
            if(this.isHero) {
                if(inventory.getItems().size() < (i+1)) {
                    this.inventoryImages[i] = new ScreenImage(this.emptyIcon, new Point(0, 0));
                    this.inventoryImages[i].setScale(this.scale);
                    this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/1.75f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
                } else {
                    this.inventoryImages[i] = new ScreenImage(inventory.getItem(i).getInventoryTexture().getNextAnimationTexturePath(), new Point(0, 0));
                    this.inventoryImages[i].setScale(this.scale);
                    this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/1.75f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
                }
            } else {
                if(inventory.getItems().size() < (i+1)) {
                    this.inventoryImages[i] = new ScreenImage(this.emptyIcon, new Point(0, 0));
                    this.inventoryImages[i].setScale(this.scale);
                    this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/15f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
                } else {
                    this.inventoryImages[i] = new ScreenImage(inventory.getItem(i).getInventoryTexture().getNextAnimationTexturePath(), new Point(0, 0));
                    this.inventoryImages[i].setScale(this.scale);
                    this.inventoryImages[i].setPosition((Constants.WINDOW_WIDTH/15f+(count*16*this.scale)), ((Constants.WINDOW_HEIGHT/1.1f)-(16*nHeight)));
                }
            }
            if(inventory.getItems().size() >= (i+1)) {
                System.out.println("blub"+i);
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
    }

    private void closeInventory() {
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
    }

    private void checkItem(int index) {
        System.out.println("blub"+index);
        if(this.isHero) {
            ItemData item = this.entity.getInventory().getItem(index);
            if(item.getItemType() != ItemType.WEAPON) {
                useItem(index);
            } else {
                weaponItem(index);
            }
            openInventory();
        } else {
            moveItem(index);
            Game.getHeroEntity().getGraphicInventory().openInventory();
            openInventory();
        }
    }

    private void useItem(int index) {

    }

    private void weaponItem(int index) {

    }

    private void moveItem(int index) {
        Hero hero =  Game.getHeroEntity();
        if(hero.getInventory().emptySlots() != 0) {
            ItemData item = this.entity.getInventory().getItem(index);
            this.entity.getInventory().removeItem(item);
            hero.getInventory().addItem(item);
        }
    }

}
