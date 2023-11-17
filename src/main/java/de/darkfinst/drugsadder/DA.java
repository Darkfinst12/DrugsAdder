package de.darkfinst.drugsadder;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
        Bukkit.getConsoleSender().sendMessage(this.getInfoComponent());
        loader.init();
    }

    @Override
    public void onDisable() {
        PluginMeta meta = getPluginMeta();
        Bukkit.getLogger().info(String.format("[%s] Disabled Version %s", meta.getName(), meta.getVersion()));
        loader.unload();
    }

    public void disable() {
        this.getServer().getPluginManager().disablePlugin(this);
    }

    public Component getInfoComponent() {
        Component component = Component.empty().asComponent();
        component = component.appendNewline().append(Component.text().color(TextColor.color(8, 201, 201)).content(" ____                  _____   _   _         "));
        component = component.appendNewline().append(Component.text().color(TextColor.color(8, 201, 201)).content("|    \\ ___ _ _ ___ ___|  _  |_| |_| |___ ___      ").append(Component.text().color(TextColor.color(0, 171, 34)).content(getPluginMeta().getName() + " ").append(Component.text().color(TextColor.color(130, 130, 130)).content(getPluginMeta().getVersion()))));
        component = component.appendNewline().append(Component.text().color(TextColor.color(8, 201, 201)).content("|  |  |  _| | | . |_ -|     | . | . | -_|  _|     ")).append(Component.text().color(TextColor.color(0, 171, 34)).content("Autor ")).append(Component.text().color(TextColor.color(130, 130, 130)).content(getPluginMeta().getAuthors().toString().replace("[", "").replace("]", "")));
        component = component.appendNewline().append(Component.text().color(TextColor.color(8, 201, 201)).content("|____/|_| |___|_  |___|__|__|___|___|___|_|       ").append(Component.text().color(TextColor.color(73, 73, 73)).content(getServer().getVersion())));
        component = component.appendNewline().append(Component.text().color(TextColor.color(8, 201, 201)).content("              |___|                          "));
        return component;
    }

}
