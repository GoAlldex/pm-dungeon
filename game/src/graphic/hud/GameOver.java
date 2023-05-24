package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;
import tools.Point;

/**
 *  LightningLineSkill
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 23.05.2023
 */
public class GameOver<T extends Actor> extends ScreenController<T> {
    //create new GameOver
    public GameOver(){
        this(new SpriteBatch());
    }

    public GameOver(SpriteBatch batch){
        super(batch);
        String text = "             Game Over\n" +
                      "Press R - to start a new Level!";
        ScreenText screenText =
            new ScreenText(
                text,
                new Point(0, 0),
                2,
                new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                    .setFontcolor(Color.RED)
                    .build());
        screenText.setFontScale(2.5f);
        screenText.setPosition(
            (Constants.WINDOW_WIDTH) / 2f - (text.length() * 2) - 40,
            (Constants.WINDOW_HEIGHT) / 2f + screenText.getHeight() - 40,
            Align.center | Align.bottom);
        add((T) screenText);
        hideGameOver();
    }

    public void showGameOver() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    public void hideGameOver() {
        this.forEach((Actor s) -> s.setVisible(false));
    }

}
