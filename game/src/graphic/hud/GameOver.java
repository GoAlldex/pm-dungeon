package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import controller.ScreenController;
import ecs.entities.Hero;
import starter.Game;
import tools.Constants;
import tools.Point;

/**
 * GameOver <br>
 * Mit diesem beiden Methoden können Spieler zwischen: restart() - Soll ein Bild auf dem UI
 * angezeigt und durch TextButtonListener die methode clicked() aufrufen, um das Spiel neu zu
 * starten. <br>
 * exit() - Das Spiel wird beendet.
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_3
 * @since 23.05.2023
 */
public class GameOver<T extends Actor> extends ScreenController<T> {

    // Attributen
    private static final String PATH_TO_GAMEOVER = "hud/GameOver.png";
    private static final String PATH_TO_RESTART = "hud/restart.png";
    private static final String PATH_TO_EXIT = "hud/exit.png";

    /** Konstruktor der Konstruktor ruft den anderen Konstruktor mit einem neuen SpriteBatch auf. */
    public GameOver() {
        this(new SpriteBatch());
    }

    /**
     * Konstruktor. Es werden die Methoden: gameover(), restart() und exit() aufgerufen. Die
     * Methoden zeichnen auf dem UI die jeweiligen assets auf.
     *
     * @param batch erwartet einen SpriteBatch Objekt
     */
    public GameOver(SpriteBatch batch) {
        super(batch);
        gameOver();
        restart();
        exit();
        hideGameOver();
    }

    /** Zeige das GameOverScreen */
    public void showGameOver() {
        this.forEach(
                (Actor s) -> {
                    s.setVisible(true);
                    s.toFront();
                });
    }

    /** Verstecke das GameOverScreen */
    public void hideGameOver() {
        this.forEach((Actor s) -> s.setVisible(false));
    }

    /** Zeichne GameOver 'GameOver' bild wird angezeigt und ein position übergeben. */
    private void gameOver() {
        ScreenImage imgGameOver = new ScreenImage(PATH_TO_GAMEOVER, new Point(0, 0));
        Point pointGameOver =
                new Point((Constants.WINDOW_WIDTH / 2f) - 120, Constants.WINDOW_HEIGHT / 2f);
        imgGameOver.setPosition(pointGameOver.x, pointGameOver.y);
        imgGameOver.setScale(1.2f);
        add((T) imgGameOver);
    }

    /**
     * Zeichne Restart 'restart' Bild wird angezeigt Position wird übergeben und ein Listener. In
     * der clicked-methode wird ein neues Level durch LevelAPI gestartet.
     */
    private void restart() {
        ScreenImage imgRestart = new ScreenImage(PATH_TO_RESTART, new Point(0, 0));
        Point pointRestart = new Point(50, 120);
        imgRestart.setPosition(pointRestart.x, pointRestart.y);
        imgRestart.setScale(1.5f);
        imgRestart.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Hero hero = (Hero) Game.getHero().get();
                        hero.clear();
                        Game.loadANewLevelIfHeroDie();
                    }
                });
        add((T) imgRestart);
    }

    /**
     * Zeichne exit 'exit' Bild wird angezeigt Position wird übergeben und ein Listener. in der
     * clicked-methode wird das Spiel mit helfe Gdx.app.exit() beendet!
     */
    private void exit() {
        ScreenImage imgExit = new ScreenImage(PATH_TO_EXIT, new Point(0, 0));
        Point pointExit = new Point(Constants.WINDOW_WIDTH - (imgExit.getWidth() + 50), 120);
        imgExit.setPosition(pointExit.x, pointExit.y);
        imgExit.setScale(1.5f);
        imgExit.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        Gdx.app.exit();
                        System.exit(0);
                    }
                });
        add((T) imgExit);
    }
}
