package de.darkfinst.drugsadder;

import lombok.AccessLevel;
import lombok.Getter;
import net.minecraft.network.protocol.game.PacketPlayOutCamera;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.minecraft.world.entity.monster.EntityEnderman;
import net.minecraft.world.entity.monster.EntitySpider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

import java.security.SecureRandom;


@Getter
public class DAEffect {

    /**
     * The type of the effect for possible types see {@link DAEffectType}
     */
    private final DAEffectType effectType;
    /**
     * The minimum duration of the effect in seconds
     */
    private final int minDuration;
    /**
     * The maximum duration of the effect in seconds
     */
    private final int maxDuration;
    /**
     * The probability of the effect to be applied
     */
    private final float probability;

    //Only for Minecraft Potion Effects - Start
    /**
     * The name of the effect for possible names see {@link PotionEffectType}
     */
    @Nullable
    private final String effectName;
    /**
     * The minimum level of the effect
     */
    @Nullable
    private final Integer minLevel;
    /**
     * The maximum level of the effect
     */
    @Nullable
    private final Integer maxLevel;
    /**
     * Whether the effect should show particles or not
     */
    @Nullable
    private final Boolean particles;
    /**
     * Whether the effect should show an icon or not for explanation see {@link PotionEffect}
     */
    @Nullable
    private final Boolean icon;
    //Only for Minecraft Potion Effects - End

    //Only for Screen Effects - Start
    /**
     * The name of the screen effect
     */
    @Nullable
    private final String screenEffect;
    //Only for Screen Effects - End

    @Getter(AccessLevel.NONE)
    private final SecureRandom secureRandom;


    /**
     * Creates a new DAEffect for a Potion Effect (Minecraft)
     *
     * @param minDuration - The minimum duration of the effect in seconds
     * @param maxDuration - The maximum duration of the effect in seconds
     * @param probability - The probability of the effect to be applied
     * @param effectName  - The name of the effect for possible names see {@link PotionEffectType}
     * @param minLevel    - The minimum level of the effect
     * @param maxLevel    - The maximum level of the effect
     * @param particles   - Whether the effect should show particles or not
     * @param icon        - Whether the effect should show an icon or not for explanation see {@link PotionEffect}
     */
    public DAEffect(int minDuration, int maxDuration, float probability, @NotNull String effectName, @NotNull Integer minLevel, @NotNull Integer maxLevel, @NotNull Boolean particles, @NotNull Boolean icon) {
        this.effectType = DAEffectType.POTION;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.probability = probability;
        this.effectName = effectName;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.particles = particles;
        this.icon = icon;
        this.screenEffect = null;

        this.secureRandom = new SecureRandom();
    }

    /**
     * Creates a new DAEffect for a Screen Effect
     *
     * @param minDuration  - The minimum duration of the effect in seconds
     * @param maxDuration  - The maximum duration of the effect in seconds
     * @param probability  - The probability of the effect to be applied
     * @param screenEffect - The name of the screen effect
     */
    public DAEffect(int minDuration, int maxDuration, float probability, @NotNull String screenEffect) {
        this.effectType = DAEffectType.SCREEN;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.probability = probability;
        this.screenEffect = screenEffect;
        this.effectName = null;
        this.minLevel = null;
        this.maxLevel = null;
        this.particles = null;
        this.icon = null;

        this.secureRandom = new SecureRandom();
    }

    /**
     * Applies the effect to the player
     *
     * @param player - The player to apply the effect to
     */
    public void applyEffect(Player player) {
        if (DAEffectType.POTION.equals(this.effectType) && (this.probability >= 100.0 || (this.secureRandom.nextFloat() * 100) <= this.probability)) {
            this.applyPotionEffect(player);
        } else if (DAEffectType.SCREEN.equals(this.effectType) && (this.probability >= 100.0 || (this.secureRandom.nextFloat() * 100) <= this.probability)) {
            this.applyScreenEffect(player);
        }
    }

    /**
     * Applies the potion effect to the player
     *
     * @param player - The player to apply the effect to
     */
    private void applyPotionEffect(Player player) {
        assert this.effectName != null;
        assert this.minLevel != null;
        assert this.maxLevel != null;
        assert this.particles != null;
        assert this.icon != null;
        int level = this.secureRandom.nextInt(this.minLevel, this.maxLevel);
        int duration = this.secureRandom.nextInt(this.minDuration, this.maxDuration);
        PotionEffectType type = PotionEffectType.getByName(this.effectName);
        assert type != null;
        PotionEffect potionEffect = new PotionEffect(type, duration, level, false, this.particles, this.icon);
        player.addPotionEffect(potionEffect);
    }

    /**
     * Applies the screen effect to the player
     *
     * @param player - The player to apply the effect to
     */
    private void applyScreenEffect(Player player) {
        //Currently not implemented
    }

    @Override
    public String toString() {
        return "DAEffect{" +
                "effectType=" + (DAEffectType.POTION.equals(this.effectType) ? this.getPotionEffectString() : this.getScreenEffectString()) +
                ", minDuration=" + minDuration +
                ", maxDuration=" + maxDuration +
                ", probability=" + probability +
                '}';
    }

    private String getPotionEffectString() {
        return "PotionEffect{" +
                "effectName='" + effectName +
                ", minLevel=" + minLevel +
                ", maxLevel=" + maxLevel +
                ", particles=" + particles +
                ", icon=" + icon +
                '}';

    }

    private String getScreenEffectString() {
        return "ScreenEffect{" +
                "effectName='" + effectName +
                '}';

    }

    public enum DAEffectType {
        POTION,
        SCREEN

    }

    public enum ScreenEffectType {
        GREEN,
        SPLIT,
        INVERT,
    }
}
