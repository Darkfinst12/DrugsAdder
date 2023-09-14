package de.darkfinst.drugsadder;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.security.SecureRandom;

public class DA extends JavaPlugin {

    public static DA getInstance;
    public static DALoader loader;
    public static DALoader log;


    public static SecureRandom secureRandom;


    @Override
    public void onLoad() {
        getInstance = this;
        loader = new DALoader(this);
        log = loader;

        secureRandom = new SecureRandom();
    }

    @Override
    public void onEnable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + " ____                  _____   _   _         ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|    \\ ___ _ _ ___ ___|  _  |_| |_| |___ ___      " + ChatColor.of(new Color(0, 171, 34)) + getDescription().getName() + " " + ChatColor.of(new Color(130, 130, 130)) + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|  |  |  _| | | . |_ -|     | . | . | -_|  _|     ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|____/|_| |___|_  |___|__|__|___|___|___|_|       " + ChatColor.of(new Color(73, 73, 73)) + getServer().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "              |___|                          ");
        loader.init();
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        loader.unload();
    }

    public void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

}
