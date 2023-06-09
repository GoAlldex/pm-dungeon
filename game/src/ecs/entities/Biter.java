package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.ai.AIComponent;
import ecs.components.ai.idle.Idle;
import ecs.components.ai.idle.PatrouilleWalk;
import ecs.components.ai.idle.RadiusWalk;
import ecs.components.ai.idle.StaticRadiusWalk;
import ecs.damage.Damage;
import ecs.items.ItemData;
import ecs.items.ItemDataGenerator;
import ecs.items.WorldItemBuilder;
import ecs.systems.CollisionSystem;
import ecs.systems.MyFormatter;
import graphic.Animation;
import level.elements.tile.FloorTile;
import starter.Game;
import tools.Constants;
import tools.Point;

import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.logging.*;

import static ecs.damage.DamageType.PHYSICAL;

/**
 <b><span style="color: rgba(3,71,134,1);">Unsere Monsterklasse "Beißer".</span></b><br>
 Hier werden die wichtigesten bestandteile unseres Monsters "Beißer" initialisiert.<br><br>

 @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 @version cycle_1
 @since 26.04.2023
 */
public class Biter extends Monster {

    /**
     <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     Initialisiert ein neues Monster "Beißer".
     @param level Typ des Monsters
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_1
     @since 26.04.2023
     */
    public Biter(int level) {
        super();
        this.position = new PositionComponent(this);
        onDeath();
        this.hitAnimation = AnimationBuilder.buildAnimation("");
        this.dieAnimation = AnimationBuilder.buildAnimation("monster/type1/death");
        this.hp = new HealthComponent(this, Math.round(15f*(1f+((float)level/10f)-0.1f)), this.death, this.hitAnimation, this.dieAnimation);
        this.hitSpeed = 3;
        this.xp = Math.round(10*(1+(level/10)-0.1f));
        this.dmg = Math.round(2*(1+(level/10)-0.1f));
        this.dmgType = 0;
        this.speed[0] = 0.1f;
        this.speed[1] = 0.1f;
        this.pathToIdleLeft = "monster/type1/idleLeft";
        this.pathToIdleRight = "monster/type1/idleRight";
        this.pathToRunLeft = "monster/type1/runLeft";
        this.pathToRunRight = "monster/type1/runRight";
        setupVelocityComponent();
        setupAnimationComponent();
        setupHitboxComponent();
        this.ai = new AIComponent(this);
        monsterMoveStrategy();
        this.ai.execute();
        setItem();
    }

    private void monsterMoveStrategy() {
        Random rnd = new Random();
        int strategy = rnd.nextInt(4);
        int radius = rnd.nextInt(8)+2;
        int checkPoints = rnd.nextInt(3)+2;
        int pauseTime = rnd.nextInt(5)+1;
        switch(strategy) {
            case 0:
                this.ai.setIdleAI(new PatrouilleWalk(radius, checkPoints, pauseTime, PatrouilleWalk.MODE.LOOP));
                break;
            case 1:
                this.ai.setIdleAI(new Idle());
                break;
            case 2:
                this.ai.setIdleAI(new RadiusWalk(radius, pauseTime));
                break;
            case 3:
                this.ai.setIdleAI(new StaticRadiusWalk(radius, pauseTime));
                break;
        }
    }

    private void setupVelocityComponent() {
        Animation moveRight = AnimationBuilder.buildAnimation(pathToRunRight);
        Animation moveLeft = AnimationBuilder.buildAnimation(pathToRunLeft);
        new VelocityComponent(this, this.speed[0], this.speed[1], moveLeft, moveRight);
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(pathToIdleRight);
        Animation idleLeft = AnimationBuilder.buildAnimation(pathToIdleLeft);
        this.monsterAnimation = new AnimationComponent(this, idleLeft, idleRight);
    }

    @Override
    public void update() {
        if(!Game.getPause()) {
            fightHero();
        }
    }

    private void fightHero() {
        if(this.fight && this.getHp().getCurrentHealthpoints() <= 0) {
            this.fight = false;
        } else {
            if (this.fight && Game.getEntities().contains(this.hero)) {
                this.frameTime = this.hitSpeed * Constants.FRAME_RATE;
                if (this.hitPause >= this.frameTime) {
                    this.hitPause = 0;
                    this.hero.getHp().receiveHit(new Damage(this.dmg, PHYSICAL, this));
                    //this.hero.getHeroAnimation().setCurrentAnimation(this.hero.getHp().getGetHitAnimation());
                    /*Boolean check_kick = false;
                    Point npos = hero.getPosition().getPosition();
                    npos.x += 0.25f;
                    for(FloorTile ft : Game.currentLevel.getFloorTiles()) {
                        if(ft.getCoordinate().equals(npos)) {
                            check_kick = true;
                            break;
                        }
                    }
                    if(check_kick) {
                        hero.getPosition().setPosition(npos);
                    }*/
                    log.info("Biter hits Hero: " + this.dmg + " Hp");
                    if (this.hero.getHp().getCurrentHealthpoints() <= 0) {
                        this.fight = false;
                    }
                } else {
                    this.hitPause++;
                }
            } else {
                this.fight = false;
            }
        }
    }

    private void setupHitboxComponent() {
        this.hitBox = new HitboxComponent(
            this,
            (you, other, direction) -> {
                if(other instanceof Hero) {
                    if(this.hero == null) {
                        this.hero = (Hero) other;
                    }
                    this.fight = true;
                }
            },
            (you, other, direction) -> {
                if(other instanceof Hero) {
                    this.fight = false;
                }
            });
    }

    private void setItem() {
        ItemDataGenerator itm = new ItemDataGenerator();
        int rnd = new Random().nextInt(itm.getAllItems().size());
        this.item = itm.getItem(rnd);
    }

    /**
     <b><span style="color: rgba(3,71,134,1);">Wenn das Monster stirbt</span></b><br>
     - Wenn das Monster stirbt deaktiviere das Kampfsystem
     - Gib dem Helden XP
     - Prüfe ob der Held durch die erhaltene XP ein level aufgestiegen ist, wenn ja setze Nahkampfschaden und HP hoch
     - Setze einen Blutfleck
     - Drop ein Item
     - Entferne das Monster vom Spiel
     @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     @version cycle_4
     @since 04.06.2023
     */
    public void onDeath() {
        this.death = entity -> {
            this.hitBox = null;
            hero.getFightHandler().remove(this);
            this.fight = false;
            log.info("Monster biter died");
            long lvl = hero.getXP().getCurrentLevel();
            hero.getXP().addXP(xp);
            if(lvl < hero.getXP().getCurrentLevel()) {
                log.info("Hero level up: "+lvl+" to "+hero.getXP().getCurrentLevel());
                hero.setMaxHp((int)hero.getXP().getCurrentLevel());
                hero.setMeleeDmg((int)hero.getXP().getCurrentLevel());
                log.info("New parameters for hero: new "+hero.getHp().getMaximalHealthpoints()+" HP and new "+hero.getDmg()+" melee DMG");
            }
            log.info("Hero get "+xp+" XP");
            Game.addEntity(new DeadAnimation(this));
            log.info("New entity dead biter");
            Game.addEntity(WorldItemBuilder.buildWorldItem(item, this.position.getPosition()));
            log.info("Biter dropped "+item.getItemName()+"");
            Game.removeEntity(this);
        };
    }

}
