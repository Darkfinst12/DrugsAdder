package de.darkfinst.DrugsAdder;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;

public class DA extends JavaPlugin {

    public static DA getInstance;
    public static DALoader loader;


    @Override
    public void onLoad() {
        getInstance = this;
        loader = new DALoader(this);
    }

    @Override
    public void onEnable() {
        loader.init();
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + " ____                  _____   _   _         ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|    \\ ___ _ _ ___ ___|  _  |_| |_| |___ ___ ");
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|  |  |  _| | | . |_ -|     | . | . | -_|  _|     " + ChatColor.of(new Color(0, 171, 34)) + getDescription().getName() + " " + ChatColor.of(new Color(73, 73, 73)) + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "|____/|_| |___|_  |___|__|__|___|___|___|_|       " + ChatColor.of(new Color(73, 73, 73)) + String.format("(MC %s)", getDescription().getAPIVersion()));
        Bukkit.getConsoleSender().sendMessage(ChatColor.of(new Color(8, 201, 201)) + "              |___|                          ");
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
