package de.darkfinst.DrugsAdder;

import de.darkfinst.DrugsAdder.filedata.DAConfig;
import de.darkfinst.DrugsAdder.listeners.SignChangeEventListener;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.awt.*;

@Getter
public class DALoader {

    private final DA plugin;

    private final DAConfig daConfig = new DAConfig();


    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.initConfig();
        this.initCommands();
        this.initListener();
    }

    private void initConfig() {
        if (!this.daConfig.checkConfig()) {
            this.plugin.disable();
        }
        this.daConfig.readConfig();
    }

    private void initCommands() {
        new DACommand().register();
    }

    private void initListener() {
        new SignChangeEventListener();
    }

    //Logging
    public void msg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.of(new Color(3, 94, 212)) + "[DrugsAdder] " + ChatColor.WHITE + msg);
    }

    public void log(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.WHITE + msg);
    }

    public void debugLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(212, 192, 3)) + "[Debug]" + ChatColor.WHITE + msg);
    }

    public void errorLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(196, 33, 33)) + "[ERROR]" + ChatColor.WHITE + msg);
    }


    public void unload() {
    }

}
