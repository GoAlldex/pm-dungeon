package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.entities.*;
import graphic.Animation;
import level.elements.tile.FloorTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnTrap extends TrapGenerator{
    private List<Monster> monsters = new ArrayList<>();
    private static final String SPAWNPATH = FLOORPATH + "monsterTrap.png";
    private static AnimationComponent animationComponentILLUSION, animationComponentSPAWN;

    private int levelCounter;

    public SpawnTrap(List<FloorTile> floorTiles, int levelCounter){
        super();
        this.levelCounter = levelCounter;
        animationComponentILLUSION = new AnimationComponent(this, AnimationBuilder.buildAnimation(ILLUSIONPATH));
        animationComponentSPAWN = new AnimationComponent(this, AnimationBuilder.buildAnimation(SPAWNPATH));
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(true);
        placedSwitch(false);
        animation();
        setupHitboxComponent();
    }

    @Override
    public Animation animation() {
        return visibility() ?  showTrap() : animationComponentILLUSION.getCurrentAnimation();
    }
    private boolean oneTime = false;
    @Override
    public Animation showTrap() {
        Animation animation = new Animation(AnimationBuilder.buildAnimation(SPAWNPATH).getAnimationFrames(), 1);
        AnimationComponent component = new AnimationComponent(this, animation);
        return animation;
    }

    @Override
    public void setupHitboxComponent() {
        new HitboxComponent(
          this,
            (a, b, direction) -> touch(),
            (a, b, direction) -> System.out.println("Leave: " + SpawnTrap.class)
        );
    }

    private void touch() {
        visibility(true);
        setTrigger(true);
        spawn();
        if (isTrigger()) {
            Animation animation = animationComponentILLUSION.getCurrentAnimation();
            AnimationComponent component = new AnimationComponent(this, animation);
        }
    }

    private void spawn() {
        if(isTrigger()){
            if (!oneTime){
                int rnd = new Random().nextInt(2) + 1;
                for (int i = 0; i < rnd; i++) {
                    int rnd_mon = new Random().nextInt(3);
                    if(rnd_mon == 0) {
                        monsters.add(new Biter(levelCounter));
                    } else if(rnd_mon == 1) {
                        monsters.add(new Zombie(levelCounter));
                    } else {
                        monsters.add(new LittleDragon(levelCounter));
                    }
                }
            }
            oneTime = true;
        }
    }
}
