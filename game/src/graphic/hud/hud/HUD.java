package graphic.hud.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import configuration.KeyboardConfig;
import controller.ScreenController;
import ecs.entities.Hero;
import ecs.items.ItemData;
import graphic.hud.*;
import graphic.hud.inventory.HeroGraphicInventory;
import starter.Game;
import tools.Constants;
import tools.Point;
import java.util.logging.Logger;
/**
 * <b><span style="color: rgba(3,71,134,1);">Unser HUD.</span></b><br>
 * Hier werden die wichtigesten bestandteile unseres HUDs initialisiert.<br>
 *
 * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 * @version cycle_5
 * @since 17.06.2023
 */
public class HUD<T extends Actor> extends ScreenController<T> {

    private Hero hero;
    private static final Logger log = Logger.getLogger(HUD.class.getName());
    /**
     * <b><span style="color: rgba(3,71,134,1);">Konstruktor</span></b><br>
     * Initialisiert des HUDs.
     *
     ** @param hero Übergibt den Helden um auf die public Methoden zugreifen zu können
     * @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
     * @version cycle_5
     * @since 17.06.2023
     */
    public HUD(Hero hero) {
        this(hero, new SpriteBatch());
    }

    public HUD(Hero hero, SpriteBatch batch) {
        super(batch);
        this.hero = hero;
        Game.controller.add(this);
        log.info("HUD initialisiert");
    }

    @Override
    public void update() {
        this.forEach((Actor s) -> s.setVisible(false));
        this.forEach((Actor s) -> s.remove());
        drawHP();
        drawMana();
        drawLevel();
        drawDungeonLevel();
        drawWeapon();
        drawSkills();
        drawBag();
        this.forEach((Actor s) -> s.setVisible(true));
        super.update();
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawHP() {
        String imgPath = "";
        /*if(hero.getHP().getCurrentHealthpoints() > (hero.getHP().getMaximalHealthpoints()/2)) {
            imgPath = "hud/ui_heart_full.png";
        } else if(hero.getHP().getCurrentHealthpoints() <= (hero.getHP().getMaximalHealthpoints()/2)) {
            imgPath = "hud/ui_heart_half.png";
        }
        ScreenImage hpImg = new ScreenImage(imgPath, new Point(0, Constants.WINDOW_HEIGHT - 32));
        hpImg.setScale(2f);
        add((T) hpImg);*/
        ScreenImage emptyBar = new ScreenImage("hud/empty_bar.png", new Point(0, Constants.WINDOW_HEIGHT - 36));
        emptyBar.setScale(2f);
        add((T) emptyBar);
        float x;
        int diff = hero.getHP().getMaximalHealthpoints()-hero.getHP().getCurrentHealthpoints();
        if(diff >= 0) {
            float factor = 2f/hero.getHP().getMaximalHealthpoints();
            x = factor*hero.getHP().getCurrentHealthpoints();
        } else {
            x = 0f;
        }
        if(hero.getHP().getCurrentHealthpoints() > (hero.getHP().getMaximalHealthpoints()/2)) {
            imgPath = "hud/hp_full.png";
        } else if(hero.getHP().getCurrentHealthpoints() <= (hero.getHP().getMaximalHealthpoints()/2) && hero.getHP().getCurrentHealthpoints() > (hero.getHP().getMaximalHealthpoints()/4)) {
            imgPath = "hud/hp_half.png";
        } else {
            imgPath = "hud/hp_empty.png";
        }
        if(hero.getHP().getCurrentHealthpoints() > 0) {
            ScreenImage hpStatus = new ScreenImage(imgPath, new Point(2, Constants.WINDOW_HEIGHT - 34));
            hpStatus.setScale(x, 2f);
            add((T) hpStatus);
        }
        ScreenText hp = new ScreenText("HP: "+hero.getHP().getCurrentHealthpoints()+"/"+hero.getHP().getMaximalHealthpoints(), new Point(32, Constants.WINDOW_HEIGHT-25),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) hp);
    }

    private void drawMana() {
        ScreenImage emptyBar = new ScreenImage("hud/empty_bar.png", new Point(0, Constants.WINDOW_HEIGHT - 74));
        emptyBar.setScale(2f);
        add((T) emptyBar);
        float x;
        int diff = hero.getMc().getMaxManaPoint()-hero.getMc().getCurrentManaPoint();
        if(diff >= 0) {
            float factor = 2f/hero.getMc().getMaxManaPoint();
            x = factor*hero.getMc().getCurrentManaPoint();
        } else {
            x = 0f;
        }
        if(hero.getMc().getCurrentManaPoint() > 0) {
            ScreenImage manaStatus = new ScreenImage("hud/mana.png", new Point(2, Constants.WINDOW_HEIGHT - 72));
            manaStatus.setScale(x, 2f);
            add((T) manaStatus);
        }
        ScreenText mana = new ScreenText("Mana: "+hero.getMc().getCurrentManaPoint()+"/"+hero.getMc().getMaxManaPoint(), new Point(32, Constants.WINDOW_HEIGHT-63),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) mana);
    }

    private void drawLevel() {
        ScreenImage levelImg = new ScreenImage("hud/level.png", new Point(Constants.WINDOW_WIDTH/2-35, 52));
        levelImg.setScale(0.4f, 0.5f);
        add((T) levelImg);
        ScreenText level = new ScreenText("Lv. "+this.hero.getLevel()+"", new Point(Constants.WINDOW_WIDTH/2-24, 58),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) level);
        float x;
        long diff = hero.getXP().getMaxXP()-hero.getXP().getCurrentXP();
        if(diff >= 0) {
            float factor = 3.7f/hero.getXP().getMaxXP();
            x = factor*hero.getXP().getCurrentXP();
        } else {
            x = 0f;
        }
        if(hero.getXP().getCurrentXP() > 0) {
            ScreenImage ep = new ScreenImage("hud/mana.png", new Point(Constants.WINDOW_WIDTH / 2 - 128, 46));
            ep.setScale(x, 0.25f);
            add((T) ep);
        }
    }

    private void drawDungeonLevel() {
        ScreenImage levelName = new ScreenImage("hud/level.png", new Point(Constants.WINDOW_WIDTH/2-82, Constants.WINDOW_HEIGHT-32));
        levelName.setScale(1f, 0.5f);
        add((T) levelName);
        ScreenText levelNameText = new ScreenText("Dungeon Level", new Point(Constants.WINDOW_WIDTH/2-66, Constants.WINDOW_HEIGHT-24),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) levelNameText);
        ScreenImage levelImg = new ScreenImage("hud/level.png", new Point(Constants.WINDOW_WIDTH/2+38, Constants.WINDOW_HEIGHT-32));
        levelImg.setScale(0.25f, 0.5f);
        add((T) levelImg);
        ScreenText level = new ScreenText(""+Game.levelCounter+"", new Point(Constants.WINDOW_WIDTH/2+48, Constants.WINDOW_HEIGHT-24),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) level);
    }

    private void drawWeapon() {
        ScreenImage weapon;
        if(hero.getWeapon() != null) {
            weapon = new ScreenImage(hero.getWeapon().getInventoryTexture().getNextAnimationTexturePath(), new Point(Constants.WINDOW_WIDTH - 48, Constants.WINDOW_HEIGHT - 48));
            weapon.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        ItemData item = hero.getWeapon();
                        hero.getInventory().addItem(item);
                        hero.setWeapon(null);
                    }
                });
        } else {
            weapon = new ScreenImage("inventory/ui_bag_empty.png", new Point(Constants.WINDOW_WIDTH - 48, Constants.WINDOW_HEIGHT - 48));
        }
        weapon.setScale(3f);
        add((T) weapon);
    }

    private void drawSkills() {
        ScreenImage[] skillSlots = new ScreenImage[6];
        for(int i = 0; i < skillSlots.length; i++) {
            skillSlots[i] = new ScreenImage("hud/skill.png", new Point(Constants.WINDOW_WIDTH/2-(128-i*40), 6));
            skillSlots[i].setScale(1f);
            add((T) skillSlots[i]);
        }
        // Skill 1
        ScreenImage skill1Img = new ScreenImage("hud/skills/fireball.png", new Point(Constants.WINDOW_WIDTH/2-117, 18));
        skill1Img.setScale(1f);
        add((T) skill1Img);
        String cd1 = "0";
        if(hero.getSkills().getSkillSlot1().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot1().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd1 = "" + Math.round(hero.getSkills().getSkillSlot1().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill1Text = new ScreenText(cd1, new Point(Constants.WINDOW_WIDTH/2-126, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill1Text);
        ScreenText skill1Key = new ScreenText(Input.Keys.toString(KeyboardConfig.FIRST_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2-108, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill1Key);
        // Skill 2
        ScreenImage skill2Img = new ScreenImage("hud/skills/lightning.png", new Point(Constants.WINDOW_WIDTH/2-77, 18));
        skill2Img.setScale(1f);
        add((T) skill2Img);
        String cd2 = "0";
        if(hero.getSkills().getSkillSlot2().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot2().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd2 = "" + Math.round(hero.getSkills().getSkillSlot2().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill2Text = new ScreenText(cd2, new Point(Constants.WINDOW_WIDTH/2-86, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill2Text);
        ScreenText skill2Key = new ScreenText(Input.Keys.toString(KeyboardConfig.SECOND_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2-68, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill2Key);
        // Skill 3
        ScreenImage skill3Img = new ScreenImage("hud/skills/mindcontrol.png", new Point(Constants.WINDOW_WIDTH/2-37, 18));
        skill3Img.setScale(1f);
        add((T) skill3Img);
        String cd3 = "0";
        if(hero.getSkills().getSkillSlot3().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot3().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd3 = "" + Math.round(hero.getSkills().getSkillSlot3().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill3Text = new ScreenText(cd3, new Point(Constants.WINDOW_WIDTH/2-46, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill3Text);
        ScreenText skill3Key = new ScreenText(Input.Keys.toString(KeyboardConfig.THIRD_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2-28, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill3Key);
        // Skill 4
        ScreenImage skill4Img = new ScreenImage("hud/skills/transform.png", new Point(Constants.WINDOW_WIDTH/2+4, 18));
        skill4Img.setScale(1f);
        add((T) skill4Img);
        String cd4 = "0";
        if(hero.getSkills().getSkillSlot4().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot4().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd4 = "" + Math.round(hero.getSkills().getSkillSlot4().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill4Text = new ScreenText(cd4, new Point(Constants.WINDOW_WIDTH/2-6, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill4Text);
        ScreenText skill4Key = new ScreenText(Input.Keys.toString(KeyboardConfig.FOURTH_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2+12, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill4Key);
        // Skill 5
        ScreenImage skill5Img = new ScreenImage("hud/skills/arrow1.png", new Point(Constants.WINDOW_WIDTH/2+44, 18));
        skill5Img.setScale(1f);
        add((T) skill5Img);
        String cd5 = "0";
        if(hero.getSkills().getSkillSlot5().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot5().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd5 = "" + Math.round(hero.getSkills().getSkillSlot5().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill5Text = new ScreenText(cd5, new Point(Constants.WINDOW_WIDTH/2+34, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill5Text);
        ScreenText skill5Key = new ScreenText(Input.Keys.toString(KeyboardConfig.FIFTH_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2+52, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill5Key);
        // Skill 6
        ScreenImage skill6Img = new ScreenImage("hud/skills/boomerang1.png", new Point(Constants.WINDOW_WIDTH/2+84, 18));
        skill6Img.setScale(1f);
        add((T) skill6Img);
        String cd6 = "0";
        if(hero.getSkills().getSkillSlot6().get().getCoolDownInFrames() != 0) {
            if ((hero.getSkills().getSkillSlot6().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) > 0) {
                cd6 = "" + Math.round(hero.getSkills().getSkillSlot6().get().getCurrentCoolDownInFrames() / Constants.FRAME_RATE) + "";
            }
        }
        ScreenText skill6Text = new ScreenText(cd6, new Point(Constants.WINDOW_WIDTH/2+74, 26),2f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.RED)
                .build());
        add((T) skill6Text);
        ScreenText skill6Key = new ScreenText(Input.Keys.toString(KeyboardConfig.SIXTH_SKILL.get()), new Point(Constants.WINDOW_WIDTH/2+92, 5),0.5f,
            new LabelStyleBuilder(FontBuilder.DEFAULT_FONT)
                .setFontcolor(Color.WHITE)
                .build());
        add((T) skill6Key);
    }

    private void drawBag() {
        ScreenImage bag =  new ScreenImage("inventory/ui_bag_icon.png", new Point(Constants.WINDOW_WIDTH - 34, 6));
        bag.setScale(2f);
        bag.addListener(new TextButtonListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(hero.getIsOpen()) {
                    if(Game.getPause()) {
                        Game.togglePause();
                    }
                    log.info("Inventar wird geschlossen");
                    hero.setIsOpen(false);
                    hero.getGraphicInventory().closeInventory();
                } else {
                    if(!Game.getPause()) {
                        Game.togglePause();
                    }
                    log.info("Inventar wird geöffnet");
                    hero.setIsOpen(true);
                    hero.setGraphicInventory(new HeroGraphicInventory(hero));
                    hero.getGraphicInventory().openInventory();
                }
            }
        });
        add((T) bag);
    }

}
