package starter;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static logging.LoggerConfig.initBaseLogger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import configuration.Configuration;
import configuration.KeyboardConfig;
import controller.AbstractController;
import controller.ScreenController;
import controller.SystemController;
import creature.trap.*;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.*;
import ecs.entities.boss.BiterBoss;
import ecs.entities.boss.Boss;
import ecs.entities.boss.OrcBoss;
import ecs.entities.boss.ZombieBoss;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import ecs.systems.*;
import graphic.DungeonCamera;
import graphic.Painter;
import graphic.hud.GameOver;
import graphic.hud.PauseMenu;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import level.IOnLevelLoader;
import level.LevelAPI;
import level.elements.ILevel;
import level.elements.tile.Tile;
import level.generator.IGenerator;
import level.generator.postGeneration.WallGenerator;
import level.generator.randomwalk.RandomWalkGenerator;
import level.tools.LevelSize;
import tools.Constants;
import tools.Point;

/** The heart of the framework. From here all strings are pulled. */
public class Game extends ScreenAdapter implements IOnLevelLoader {

    private static final LevelSize LEVELSIZE = LevelSize.SMALL;

    /**
     * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
     * batch.
     */
    protected SpriteBatch batch;

    /** Contains all Controller of the Dungeon */
    public static List<AbstractController<?>> controller;

    public static DungeonCamera camera;
    /** Draws objects */
    protected Painter painter;

    protected static LevelAPI levelAPI;
    /** Generates the level */
    protected IGenerator generator;

    private boolean doSetup = true;
    private static boolean paused = false, isGameOver = false;

    /** All entities that are currently active in the dungeon */
    private static final Set<Entity> entities = new HashSet<>();
    /** All entities to be removed from the dungeon in the next frame */
    private static final Set<Entity> entitiesToRemove = new HashSet<>();
    /** All entities to be added from the dungeon in the next frame */
    private static final Set<Entity> entitiesToAdd = new HashSet<>();

    /** List of all Systems in the ECS */
    public static SystemController systems;

    public static ILevel currentLevel;
    private static PauseMenu<Actor> pauseMenu;
    private static GameOver<Actor> gameOver;
    private static Entity hero;
    private Logger gameLogger;
    private static ArrayList<Monster> monster = new ArrayList<>();
    public static int levelCounter = 0;
    private static final List<TrapGenerator> trapGenerators = new ArrayList<>();
    private ArrayList<Entity> worldItems = new ArrayList<>();
    private boolean inventoryOpen = false;
    private ArrayList<Entity> inventory = new ArrayList<>();
    private static ArrayList<NPC> npcs = new ArrayList<>();
    private static Tomb tomb = null;
    public static ArrayList<Boss> bosses = new ArrayList<>();

    public static void main(String[] args) {
        // start the game
        try {
            Configuration.loadAndGetConfiguration("dungeon_config.json", KeyboardConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DesktopLauncher.run(new Game());
    }

    /**
     * Main game loop. Redraws the dungeon and calls the own implementation (beginFrame, endFrame
     * and onLevelLoad).
     *
     * @param delta Time since last loop.
     */
    @Override
    public void render(float delta) {
        if (doSetup) setup();
        batch.setProjectionMatrix(camera.combined);
        frame();
        clearScreen();
        levelAPI.update();
        controller.forEach(AbstractController::update);
        camera.update();
    }

    /** Called once at the beginning of the game. */
    protected void setup() {
        doSetup = false;
        controller = new ArrayList<>();
        setupCameras();
        painter = new Painter(batch, camera);
        generator = new RandomWalkGenerator();
        levelAPI = new LevelAPI(batch, painter, generator, this);
        initBaseLogger();
        gameLogger = Logger.getLogger(this.getClass().getName());
        systems = new SystemController();
        controller.add(systems);
        pauseMenu = new PauseMenu<>();
        gameOver = new GameOver<>();
        controller.add(pauseMenu);
        controller.add(gameOver);
        hero = new Hero();
        levelAPI = new LevelAPI(batch, painter, new WallGenerator(new RandomWalkGenerator()), this);
        levelAPI.loadLevel(LEVELSIZE);
        createSystems();
    }

    public static boolean getPause() {
        return paused;
    }

    /** Called at the beginning of each frame. Before the controllers call <code>update</code>. */
    protected void frame() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
            if (inventoryOpen == false) {
                // inventory.add(WorldInventoryBuilder.buildWorldInventory(hero.getInventory(), new
                // Point(0,0)));
            } else {
                // inventory.clear();
            }
        }
        setCameraFocus();
        manageEntitiesSets();
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) togglePause();
        tomb.update(levelCounter);
        for (Monster m : monster) {
            m.update();
        }
        hero.update();
        if (hero != null && getHero().isPresent()) {
            Hero hero1 = (Hero) hero;
            if (hero1.pc != null) {
                // Skill 1 - Fireball
                if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                    if (hero1.pc.getSkillSlot1().isPresent())
                        hero1.execute(hero1.pc.getSkillSlot1().get());
                }
                // Skill 2 - Blitzschlag
                if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                    if (hero1.pc.getSkillSlot2().isPresent())
                        hero1.execute(hero1.pc.getSkillSlot2().get());
                }
                // Skill 3 - Verwandlung
                if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                    if (hero1.pc.getSkillSlot3().isPresent())
                        hero1.execute(hero1.pc.getSkillSlot3().get());
                }
                // Skill 4 Boss Informationen
                if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
                    if (hero1.pc.getSkillSlot4().isPresent())
                        hero1.execute(hero1.pc.getSkillSlot4().get());
                }
                // soldange nicht gameover ist, Rufe die methode
                // update auf
                if (!hero1.isGameOver()) {
                    hero1.update();
                    for (Boss boss : bosses) {
                        if (entities.contains(boss)) {
                            boss.update(entities, levelCounter);
                        }
                    }
                }
                // wenn gameover und hero noch da ist,
                // Lösche es
                if (hero1.isGameOver()) {
                    if (Game.getHero().isPresent()) {
                        levelCounter = 0;
                        entities.clear();
                        bosses.clear();
                        monster.clear();
                    }
                }
            }
        }
        tomb.update(entities, levelCounter);
        getHero().ifPresent(this::loadNextLevelIfEntityIsOnEndTile);
    }

    @Override
    public void onLevelLoad() {
        trapGenerators.clear();
        currentLevel = levelAPI.getCurrentLevel();
        levelCounter++;
        entities.clear();
        createNPC();
        //createMonster();
        createWorldItems();
        int randomNumberTraps = new Random().nextInt(2);
        for (int i = 0; i < randomNumberTraps; i++) {
            // trapGenerator
            trapGenerators.add(new SpikesTrap(currentLevel.getFloorTiles()));
            trapGenerators.add(new TeleportTrap(currentLevel.getFloorTiles(), hero));
            trapGenerators.add(new SpawnTrap(currentLevel.getFloorTiles(), levelCounter));
        }
        getTraps().ifPresent(this::placeForTraps);
        getHero().ifPresent(this::placeOnLevelStart);
    }

    public void createNPC() {
        tomb = null;
        npcs.clear();
        Ghost nGhost = new Ghost();
        tomb = new Tomb(nGhost);
        npcs.add(nGhost);
        for (NPC n : npcs) {
            entities.add(n);
            PositionComponent npc =
                    (PositionComponent)
                            n.getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            npc.setPosition(currentLevel.getRandomFloorTile().getCoordinate().toPoint());
        }
        entities.add(tomb);
    }

    public void createWorldItems() {
        Random rnd = new Random();
        int rnd_itm_anz = rnd.nextInt(2);
        rnd_itm_anz++;
        ItemDataGenerator itm = new ItemDataGenerator();
        for (int i = 0; i < rnd_itm_anz; i++) {
            worldItems.add(
                    WorldItemBuilder.buildWorldItem(
                            itm.generateItemData(),
                            currentLevel.getRandomFloorTile().getCoordinate().toPoint()));
        }
    }

    public void createMonster() {
        monster.clear();
        Random rnd = new Random();
        int rnd_mon_anz = rnd.nextInt(4);
        rnd_mon_anz++;
        for (int i = 0; i < rnd_mon_anz; i++) {
            int rnd_mon = new Random().nextInt(3);
            if (rnd_mon == 0) {
                monster.add(new Biter(levelCounter));
            } else if (rnd_mon == 1) {
                monster.add(new Zombie(levelCounter));
            } else {
                monster.add(new LittleDragon(levelCounter));
            }
        }
        for (Monster m : monster) {
            entities.add(m);
            PositionComponent npc =
                    (PositionComponent)
                            m.getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            npc.setPosition(currentLevel.getRandomFloorTile().getCoordinate().toPoint());
        }
        ZombieBoss zombieBoss = new ZombieBoss(levelCounter);
        BiterBoss biterBoss = new BiterBoss(levelCounter);
        OrcBoss orcBoss = new OrcBoss(levelCounter);
        zombieBoss.setPosition(getPositionComponent(zombieBoss));
        biterBoss.setPosition(getPositionComponent(biterBoss));
        orcBoss.setPosition(getPositionComponent(orcBoss));
        bosses.add(zombieBoss);
        bosses.add(biterBoss);
        bosses.add(orcBoss);
    }

    private PositionComponent getPositionComponent(Entity entity) {
        PositionComponent position =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        addEntity(entity);
        return position;
    }

    public static Optional<Entity> getTraps() {
        if (trapGenerators.size() > 0) {
            for (TrapGenerator trapGenerator : trapGenerators) {
                return Optional.ofNullable(trapGenerator);
            }
        }
        return Optional.empty();
    }

    private void placeForTraps(Entity entity) {
        for (TrapGenerator trap : trapGenerators) {
            if (trap.visibility()) trap.visibility(false);
        }
        entities.addAll(trapGenerators);
        for (TrapGenerator trap : trapGenerators) {
            PositionComponent pc =
                    (PositionComponent)
                            trap.getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            trap.setFloorTiles(currentLevel.getFloorTiles());
            trap.generatePosition();
            pc.setPosition(trap.position().getPosition());
        }
    }

    private void manageEntitiesSets() {
        entities.removeAll(entitiesToRemove);
        entities.addAll(entitiesToAdd);
        for (Entity entity : entitiesToRemove) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was deleted.");
        }
        for (Entity entity : entitiesToAdd) {
            gameLogger.info("Entity '" + entity.getClass().getSimpleName() + "' was added.");
        }
        entitiesToRemove.clear();
        entitiesToAdd.clear();
    }

    private void setCameraFocus() {
        if (getHero().isPresent()) {
            PositionComponent pc =
                    (PositionComponent)
                            getHero()
                                    .get()
                                    .getComponent(PositionComponent.class)
                                    .orElseThrow(
                                            () ->
                                                    new MissingComponentException(
                                                            "PositionComponent"));
            camera.setFocusPoint(pc.getPosition());

        } else camera.setFocusPoint(new Point(0, 0));
    }

    private void loadNextLevelIfEntityIsOnEndTile(Entity hero) {
        if (isOnEndTile(hero)) levelAPI.loadLevel(LEVELSIZE);
    }

    private boolean isOnEndTile(Entity entity) {
        PositionComponent pc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        Tile currentTile = currentLevel.getTileAt(pc.getPosition().toCoordinate());
        return currentTile.equals(currentLevel.getEndTile());
    }

    private void placeOnLevelStart(Entity hero) {
        entities.add(hero);
        PositionComponent pc =
                (PositionComponent)
                        hero.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        pc.setPosition(currentLevel.getStartTile().getCoordinate().toPoint());
    }

    /** Toggle between pause and run */
    public static void togglePause() {
        paused = !paused;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (pauseMenu != null) {
            if (paused) pauseMenu.showMenu();
            else pauseMenu.hideMenu();
        }
    }

    /**
     * In der HUD->GameOver Klasse->restart()-methode wird 'loadANewLevelIfHeroDie' aufgerufen, um
     * das Hero Objekt neu zuladen. GameOver() methode in der Game-Klasse wird aufgerufen, da alle
     * entities gefroren sind und dies nicht der Fall ist.
     */
    public static void loadANewLevelIfHeroDie() {
        Hero meinHero = (Hero) hero;
        if (meinHero.isGameOver()) {
            hero = new Hero();
            levelAPI.loadLevel(LEVELSIZE);
            gameOver();
        }
    }

    /**
     * GameOver. Die entities werden angehalten, sobald der Hero stirbt. Im zweiten bedienung wird
     * der GameOverScreen aufgerufen und angezeigt, falls die boolesche Variable: isGameOver wahr
     * ist.
     */
    public static void gameOver() {
        isGameOver = !isGameOver;
        if (systems != null) {
            systems.forEach(ECS_System::toggleRun);
        }
        if (gameOver != null) {
            if (isGameOver) gameOver.showGameOver();
            else gameOver.hideGameOver();
        }
    }

    /**
     * Given entity will be added to the game in the next frame
     *
     * @param entity will be added to the game next frame
     */
    public static void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    /**
     * Given entity will be removed from the game in the next frame
     *
     * @param entity will be removed from the game next frame
     */
    public static void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    /**
     * @return Set with all entities currently in game
     */
    public static Set<Entity> getEntities() {
        return entities;
    }

    /**
     * @return Set with all entities that will be added to the game next frame
     */
    public static Set<Entity> getEntitiesToAdd() {
        return entitiesToAdd;
    }

    /**
     * @return Set with all entities that will be removed from the game next frame
     */
    public static Set<Entity> getEntitiesToRemove() {
        return entitiesToRemove;
    }

    /**
     * @return the player character, can be null if not initialized
     */
    public static Optional<Entity> getHero() {
        return Optional.ofNullable(hero);
    }

    /**
     * set the reference of the playable character careful: old hero will not be removed from the
     * game
     *
     * @param hero new reference of hero
     */
    public static void setHero(Entity hero) {
        Game.hero = hero;
    }

    public void setSpriteBatch(SpriteBatch batch) {
        this.batch = batch;
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
    }

    private void setupCameras() {
        camera = new DungeonCamera(null, Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT);
        camera.zoom = Constants.DEFAULT_ZOOM_FACTOR;

        // See also:
        // https://stackoverflow.com/questions/52011592/libgdx-set-ortho-camera
    }

    private void createSystems() {
        new VelocitySystem();
        new DrawSystem(painter);
        new PlayerSystem();
        new AISystem();
        new CollisionSystem();
        new HealthSystem();
        new XPSystem();
        new SkillSystem();
        new ProjectileSystem();
        Monster.MonsterLogs();
        Hero.HeroLogs();
        new ManaSystem();
    }
}
