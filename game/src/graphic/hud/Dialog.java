package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;
import tools.Point;

/**
 * <b><span style="color: rgba(3,71,134,1);">Unsere Dialog Klasse.</span></b><br>
 * Hier wird der Dialog erzeugt.<br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_4
 * @since 07.06.2023
 */

public class Dialog <T extends Actor> extends ScreenController<T> {

    private ScreenInput scin;
    private ScreenText text;
    private int dialog_entry = 0;

    /**
     * Creates a Screencontroller with a ScalingViewport which stretches the ScreenElements on
     * resize
     *
     * @param batch the batch which should be used to draw with
     */
    public Dialog(SpriteBatch batch) {
        super(batch);
    }

    public Dialog() {
        this(new SpriteBatch());
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Erstaufruf des Dialogs.</span></b><br>
     * Hier wird der Dialog angezeigt.<br>
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_4
     * @since 07.06.2023
     */
    public void createPanel() {
        ScreenImage img = new ScreenImage("./game/assets/window/window.png", new Point(0,0));
        img.setScaleX(0.5f);
        img.setScaleY(0.5f);
        img.setPosition(
            ((Constants.WINDOW_WIDTH) / 1.25f),
            ((Constants.WINDOW_HEIGHT)/ 6f),Align.center | Align.bottom);
        add((T)img);
        text = new ScreenText("Sprich mich nicht an!", new Point(0,0),1f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        text.setPosition(
            ((Constants.WINDOW_WIDTH) / 5.6f),
            ((Constants.WINDOW_HEIGHT) / 1.3f),
            Align.left | Align.topLeft);
        add((T)text);
        scin = new ScreenInput("Dein Text...", new Point(0, 0));
        scin.setPosition(
            ((Constants.WINDOW_WIDTH) / 1f - scin.getWidth()),
            ((Constants.WINDOW_HEIGHT) / 6f + scin.getHeight()),
            Align.center | Align.bottom);
        add((T) scin);
    }

    /**
     * <b><span style="color: rgba(3,71,134,1);">Antwort Text vom Geist</span></b><br>
     * Hier werden die Antworten geprüft.<br>
     * <br>
     *
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_4
     * @since 07.06.2023
     */
    public void setText() {
        switch(dialog_entry) {
            default:
                text.setText("Was?");
                scin.setText("Dein Text...");
                break;
            case 0:
                if(scin.getText().trim().equals("Ich brauche was kniffliges")) {
                    text.setText("Der 02.02.2000 war ein Tag an dem das Datum nur gerade\nZiffern enthalten hat. Wann war das das letzte Mal davor so?");
                    scin.setText("Dein Text...");
                    dialog_entry++;
                } else {
                    text.setText("Was?");
                    scin.setText("Dein Text...");
                }
                break;
            case 1:
                if(scin.getText().trim().equals("28.08.888")) {
                    text.setText("Richtig!");
                    scin.setText("Dein Text...");
                    dialog_entry = 0;
                } else {
                    text.setText("Falsch!");
                    scin.setText("Dein Text...");
                }
                break;
        }
    }

}
