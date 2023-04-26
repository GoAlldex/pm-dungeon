package creature.trap;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.entities.*;
import graphic.Animation;
import level.elements.tile.FloorTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpawnTrap extends TrapGenerator{
    private List<Monster> monsters = new ArrayList<>();
    private static final String SPAWNPATH = FLOORPATH + "monsterTrap.png";
    private static final String ILLUSIONPATH = FLOORPATH + "floor_1.png";

    private int levelCounter;

    public SpawnTrap(List<FloorTile> floorTiles, int levelCounter){
        super();
        this.levelCounter = levelCounter;
        setFloorTiles(floorTiles);
        generatePosition();
        visibility(false);
        placedSwitch(false);
        animation();
        setupHitboxComponent();
    }

    @Override
    public Animation animation() {
        return visibility() ? showTrap() : hiddenTrap();
    }
    private boolean oneTime = false;
    @Override
    public Animation showTrap() {
        if(visibility()){
            Animation animation = AnimationBuilder.buildAnimation(SPAWNPATH);
            new AnimationComponent(this, animation);
            if (isTrigegr()){
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
            return animation;
        }else {
            return hiddenTrap();
        }
    }

    @Override
    public Animation hiddenTrap() {
        if (!visibility()){
            Animation animation = AnimationBuilder.buildAnimation(ILLUSIONPATH);
            new AnimationComponent(this, animation);
            return animation;
        }else {
            return showTrap();
        }
    }

    @Override
    public void setupHitboxComponent() {
        super.setupHitboxComponent();
        setTrigegr(true);
    }

}
