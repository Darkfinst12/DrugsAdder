package main.java.de.DrugsAdder;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
        loader.unload();
    }

    public void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }


    public DALoader getLoader() {
        return loader;
    }

}
