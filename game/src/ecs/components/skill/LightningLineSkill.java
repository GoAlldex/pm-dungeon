package ecs.components.skill;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import tools.Point;
import java.util.Random;

/**
 *  LightningLineSkill
 *
 *  @author Alexey Khokhlov, Michel Witt, Ayaz Khudhur
 *  @version cycle_3
 *  @since 22.05.2023
 */
public class LightningLineSkill extends DamageProjectileSkill{
    private int min = 5, max = 10; //min. max
    private int breakTime = new Random().nextInt(max - min) + min; //breakTime
    private int mana = 2; //Mana abzüge
    private int damage = 5; //Schadenpunkte
    private String pathToLightning = "skills/lightningSkill/"; //Textur datei
    public LightningLineSkill(ITargetSelection targetSelection) {
        super(
            "",
            0.8f,
            null,
            new Point(0,0),
            targetSelection,
            new Random().nextInt(5)+1
        );
        setPathToTexturesOfProjectile(pathToLightning);
        setProjectileDamage(new Damage(damage, DamageType.MAGIC, null));
    }

    /**
     * @return CoolDownTime wird zurückgeliefert
     */
    public int getBreakTime() {
        return breakTime;
    }

    /**
     * @return Wie viel Mana werden abgezogen
     */
    public int getMana() {
        return mana;
    }

    /**
     * @return Wie viel Damages es macht
     */
    public int getDamage() {
        return damage;
    }
}
