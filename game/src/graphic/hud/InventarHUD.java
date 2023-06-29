package graphic.hud;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import ecs.entities.Hero;
import starter.Game;
import tools.Constants;
import tools.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Es wird mit der Taste I, dass Inventar geöffnet oder geschlossen.
 * Das Inventar ist für den Spieler sichtbar und kann gegenstände verwenden.
 * zsb. Der Trank regeneriert die Spieler HP um 10 %.
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version V1
 * @since 29.06.2023
 */
public class InventarHUD <T extends Actor> extends ScreenController<T> {

    private static final float INVENTORY_WIDTH_HEIGHT = 32f; //Inventar Größe
    private static final String PATH_TO_INVENTORY_SHOW = "hud/ui/inventory_platz.png"; //Inventar Hintergrundbild
    private static final String PATH_TO_INVENTORY = "hud/ui/inventory.png"; //Inventar Hintergrund
    private final List<ScreenImage> items = new ArrayList<>(); //Gesammelte Items Bild
    private final List<ScreenImage> inventoryImage = new ArrayList<>(); //Inventar platz
    private ScreenImage inventoryBackground; //Inventar Hintergrund
    private boolean setup = true; //Setup für initialisierung
    private boolean inventarOpen; //ob der inventar geöffnet oder geschlossen ist
    private Hero hero; //Hero Objekt

    /**
     * Default Konstruktor ohne Parameter
     */
    public InventarHUD(){
        this(new SpriteBatch());
    }

    /**
     * Ruft die methoden:
     * -> setupInventoryBackground() - Hintergrundbild für Inventar
     * -> setupBackground() - Inventar Platz hintergrund
     * @param batch SpriteBatch Objekt wird erwartet
     */
    public InventarHUD(SpriteBatch batch){
        super(batch);
        setupInventoryBackground();
        setupInventoryPlace();
        hidden();
    }

    /**
     * Zeigt das Inventar an.
     */
    public void show(){
        this.forEach(
            (Actor s) ->{
                s.setVisible(true);
                s.toFront();
                setInventarOpen(true);
            }
        );
    }

    /**
     * Inventar wird versteckt, bzw. unsichtbar gemacht.
     */
    public void hidden(){
        this.forEach(
            (Actor s) ->{
                s.setVisible(false);
                setInventarOpen(false);
            }
        );
    }

    /**
     * wird bei jedes Frame aufgerufen, falls in das Inventar etwas sich geändert hat.
     * Beispiel:
     * -> Item benutzen
     * -> Item sammeln
     * -> Live Ansicht beim Einsammeln.
     */
    public void updateInventory(){
        if (inventarOpen){
            if (hero == null){
                if (Game.getHero().isPresent()){
                    hero = (Hero) Game.getHero().get();
                }
            }
            if (setup){
                assert hero != null;
                setupInventory();
                setup = !setup;
            }
            if (!setup){
                assert hero != null;
                if (hero.getInventory().getItems().size() != items.size()){
                    setupInventory();
                }
                if (hero.getInventory().getItems().size() == 0){
                    loadReset();
                }
                addListener();
            }
        }
    }

    private boolean reset = false;
    private void loadReset(){
        if (reset){
            reset = false;
        }
        resetbackground();
        reset = !reset;
    }

    /*
        Hintergrundbild für Inventory einfügen
     */
    private void setupInventoryBackground(){
        inventoryBackground =
            new ScreenImage(
                PATH_TO_INVENTORY_SHOW,
                new Point(0,0)
            );
        inventoryBackground.setPosition(
            Constants.WINDOW_WIDTH / 2f + 10,
            Constants.WINDOW_HEIGHT / 2f
        );
        inventoryBackground.setScaleX(1.07f);
        inventoryBackground.setScaleY(.90f);
        inventoryBackground.getColor().a = .7f;

        add((T) inventoryBackground);
    }

    /*
    Inventar Platz wird initialisiert und hinzugefügt.
    Sichtbarkeit für Items sind max. 10
 */
    private void setupInventoryPlace(){
        for (int i = 0; i < 5; i++) {
            if (i == 0){
                addInventory(
                    getXInventory(), getYInventory(), inventoryScale(), inventoryAlpha());
            }else{
                addInventory(
                    getXInventory() + inventorySize() * i, getYInventory(), inventoryScale(), inventoryAlpha());
            }
        }
        for (int i = 0; i < 5; i++) {
            if (i == 0){
                addInventory(
                    getXInventory(), getYInventory() + inventorySize(), inventoryScale(), inventoryAlpha());
            }else{
                addInventory(
                    getXInventory() + inventorySize() * i, getYInventory() + inventorySize(), inventoryScale(), inventoryAlpha());
            }
        }
        if (inventoryImage != null){
            for (ScreenImage image : inventoryImage) {
                add((T) image);
            }
        }
    }

    /*
        Inventar Items werden initialisiert
     */
    private void setupInventory(){
        dynamicAddItems();
        addItems();
    }


    /*
        Items werden in dieser Methode automatisch eingefügt.
        oder wenn der Hero default items hat, dies werden ebenso eingefügt.
     */
    private void dynamicAddItems(){
        for (int i = 0; i < hero.getInventory().getItems().size(); i++) {
            addItem(
                hero.getInventory().getItem(i).getInventoryTexture().getNextAnimationTexturePath(),
                hero.getInventory().getItem(i).getItemName(),
                inventoryImage.get(i).getX(),
                inventoryImage.get(i).getY(),
                inventoryImage.get(i).getScaleX(),
                i
            );
        }
    }

    /*
        Items dem Controller hinzufügen
     */
    private void addItems(){
        if (items != null){
            for (ScreenImage item : items) {
                add((T) item);
            }
        }
    }

    /*
        Hilfsmethode.
        Inventar Platz an der x und y Position anzeigen.
        Skalierbarkeit und alpha übergeben.
     */
    private void addInventory(float x, float y, float scale, float a){
        ScreenImage image =
            new ScreenImage(
                PATH_TO_INVENTORY,
                new Point(0,0)
            );
        image.setPosition(
            x,
            y
        );
        image.setScale(scale);
        image.getColor().a = a;
        inventoryImage.add(image);
    }

    /*
        Items an der inventar Platz hinzufügen.
     */
    private void addItem(String itemImage, String itemName, float x, float y, float scale, int index){
        ScreenImage item =
            new ScreenImage(itemImage,
                new Point(0,0)
            );
        item.setPosition(x, y);
        item.setScale(scale * 2);
        item.getColor().a = 1;
        item.setName(itemName);
        item.toFront();
        items.add(item);
    }

    /*
        Items klickbar machen.
        Sobald auf ein Item geklickt wird, wird überprüft, ob das
        Item der richtige ist und ruft die Methode useItems in Hero Klasse auf.
        Außerdem wird das Item aus dem Inventar gelöscht.
     */
    private void addListener(){
        hero.getInventory().getItems().forEach(s -> {
            for (ScreenImage item : items) {
                item.addListener(new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (item.getName().equalsIgnoreCase(s.getItemName())){
                            if(hero.useItems(s)){
                                removeItem(item);
                            }
                        }
                    }
                });
            }
        });
    }


    /*
        Ob das Inventar geöffnet oder geschlossen ist.
     */
    private void setInventarOpen(boolean open){
        this.inventarOpen = open;
    }

    /**
     * Ob das Inventar geöffnet oder geschlossen ist.
     * Dies kann man nutzen um das Inventar Anzeigen oder schlissen.
     * @return Wahr wenn das Inventar geöffnet ist, ansonsten ist es falsch.
     */
    public boolean inventarOpen(){
        return inventarOpen;
    }

    /*
        Item image wird aus dem Inventar entfernt.
     */
    private void removeItem(ScreenImage item){
        items.remove(item);
        remove((T) item);
    }

    /*
        Das ganze inventar wird zurückgesetzt.
     */
    private void resetbackground() {
        remove((T) inventoryBackground);
        inventoryImage.clear();
        items.clear();
        setupInventoryBackground();
        setupInventoryPlace();
        setupInventory();
    }

    /*
        Inventar X Position
     */
    private float getXInventory(){
        return Constants.WINDOW_WIDTH / 2f + 10 + 4;
    }

    /*
        Inventar Y Position
     */
    private float getYInventory(){
        return Constants.WINDOW_HEIGHT / 2f + 4;
    }

    /*
        Alpha für Inventory Platz Bild
     */
    private float inventoryAlpha(){
        return .7f;
    }

    /*
        Inventory Skalierung
     */
    private float inventoryScale(){
        return .7f;
    }

    /*
        Inventory Größe
     */
    private float inventorySize(){
        return INVENTORY_WIDTH_HEIGHT * inventoryScale() + 4;
    }
}
