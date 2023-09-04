package de.darkfinst.drugsadder;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;


@Getter
public class DAEffect {

    private final DAEffectType effectType;
    private final int minDuration;
    private final int maxDuration;
    private final float probability;

    //Only for Minecraft Potion Effects
    @Nullable
    private final String effectName;
    @Nullable
    private final Integer minLevel;
    @Nullable
    private final Integer maxLevel;
    @Nullable
    private final Boolean particles;
    @Nullable
    private final Boolean icon;

    //Only for Screen Effects
    @Nullable
    private final String screenEffect;

    @Getter(AccessLevel.NONE)
    private final SecureRandom secureRandom;


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

    public void applyEffect(Player player) {
        if (DAEffectType.POTION.equals(this.effectType) && (this.probability >= 100.0 || (this.secureRandom.nextFloat() * 100) <= this.probability)) {
            this.applyPotionEffect(player);
        } else if (DAEffectType.SCREEN.equals(this.effectType) && (this.probability >= 100.0 || (this.secureRandom.nextFloat() * 100) <= this.probability)) {
            this.applyScreenEffect(player);
        }
    }

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

    private void applyScreenEffect(Player player) {
        //TODO: Find out how screen Effects work (minecraft shaders)
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
}
