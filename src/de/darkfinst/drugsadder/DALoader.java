package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.LanguageReader;
import de.darkfinst.drugsadder.listeners.*;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
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
            this.logException(e);
        }
    }

    private void initCommands() {
        new DACommand().register();
    }

    private void initListener() {
        new CraftItemEventListener();
        new FurnaceBurnEventListener();
        new FurnaceSmeltEventListener();
        new FurnaceStartSmeltEventListener();
        new PlayerInteractEventListener();
        new PrepareItemCraftEventListener();
        new SignChangeEventListener();

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
        this.msg(sender, msg, DrugsAdderSendMessageEvent.Type.NONE);
    }

    public void msg(CommandSender sender, String msg, DrugsAdderSendMessageEvent.Type Type) {
        DrugsAdderSendMessageEvent sendMessageEvent = new DrugsAdderSendMessageEvent(false, sender, msg, Type);
        this.plugin.getServer().getPluginManager().callEvent(sendMessageEvent);
        if (!sendMessageEvent.isCancelled()) {
            sender.sendMessage(ChatColor.of(new Color(3, 94, 212)) + "[DrugsAdder] " + ChatColor.WHITE + sendMessageEvent.getMessage());
        }
    }

    public void log(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.LOG);
    }

    public void debugLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(212, 192, 3)) + "[Debug] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.DEBUG);
    }

    public void errorLog(String msg) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(196, 33, 33)) + "[ERROR] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.ERROR);
    }

    public void logException(Exception e) {
        String s = e.getMessage() == null ? "null" : e.getMessage();
        StringBuilder log = new StringBuilder(s);
        Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> log.append("\n       ").append(stackTraceElement.toString()));
        this.errorLog(log.toString());
        this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
    }

    public void reloadConfig() {
        //TODO: fix this
        DAConfig.customItemReader.getRegisteredItems().clear();
        DAConfig.daRecipeReader.getRegisteredRecipes().clear();
        DAConfig.drugReader.getRegisteredDrugs().clear();
        this.initConfig();
    }


    public void unload() {
    }

}
