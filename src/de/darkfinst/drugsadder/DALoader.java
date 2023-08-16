package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.LanguageReader;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.listeners.PlayerInteractEventListener;
import de.darkfinst.drugsadder.listeners.SignChangeEventListener;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class DALoader {

    private final DA plugin;

    private DAConfig daConfig;

    public LanguageReader languageReader;
    public String language;

    private final ArrayList<DAStructure> structureList = new ArrayList<>();


    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.initConfig();
        this.initCommands();
        this.initListener();
    }

    private void initConfig() {
        try {
            FileConfiguration config = DAConfig.loadConfigFile();
            if (config == null) {
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
                return;
            }
            DAConfig.readConfig(config);
        } catch (Exception e) {
            this.errorLog(e.getMessage());
            Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> this.log(stackTraceElement.toString()));
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }

    private void initCommands() {
        new DACommand().register();
    }

    private void initListener() {
        new SignChangeEventListener();
        new PlayerInteractEventListener();

    }

    public void registerDAStructure(DAStructure structure) {
        this.structureList.add(structure);
    }

    public void unregisterDAStructure(DAStructure structure) {
        this.structureList.remove(structure);
    }

    public boolean isStructure(Block block) {
        return this.structureList.stream().anyMatch(daStructure -> daStructure.isBodyPart(block));
    }

    public DAStructure getStructure(Block block) {
        return this.structureList.stream().filter(daStructure -> daStructure.isBodyPart(block)).findAny().orElse(null);
    }

    public void openStructure(Block block, Player player) {
        DAStructure daStructure = this.getStructure(block);
        if (daStructure instanceof DABarrel daBarrel) {
            daBarrel.open(player);
        } else if (daStructure instanceof DATable daTable) {
            daTable.open(player);
        } else if (daStructure instanceof DAPress daPress) {
            daPress.usePress(player);
        }
    }


    //Logging
    public void msg(CommandSender sender, String msg) {
        DrugsAdderSendMessageEvent sendMessageEvent = new DrugsAdderSendMessageEvent(false, sender, msg);
        this.plugin.getServer().getPluginManager().callEvent(sendMessageEvent);
        if (!sendMessageEvent.isCancelled()) {
            sender.sendMessage(ChatColor.of(new Color(3, 94, 212)) + "[DrugsAdder] " + ChatColor.WHITE + sendMessageEvent.getMessage());
        }
    }

    public void log(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.WHITE + msg);
    }

    public void debugLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(212, 192, 3)) + "[Debug] " + ChatColor.WHITE + msg);
    }

    public void errorLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(196, 33, 33)) + "[ERROR] " + ChatColor.WHITE + msg);
    }


    public void unload() {
    }

}
