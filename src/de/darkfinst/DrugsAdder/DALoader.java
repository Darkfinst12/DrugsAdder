package de.darkfinst.DrugsAdder;

import de.darkfinst.DrugsAdder.Structure.barrel.DABarrel;
import de.darkfinst.DrugsAdder.Structure.DAStructure;
import de.darkfinst.DrugsAdder.Structure.press.DAPress;
import de.darkfinst.DrugsAdder.Structure.table.DATable;
import de.darkfinst.DrugsAdder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.DrugsAdder.filedata.DAConfig;
import de.darkfinst.DrugsAdder.listeners.PlayerInteractEventListener;
import de.darkfinst.DrugsAdder.listeners.SignChangeEventListener;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.ArrayList;

@Getter
public class DALoader {

    private final DA plugin;

    private DAConfig daConfig;

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
        this.daConfig = new DAConfig();
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
        new PlayerInteractEventListener();

    }

    public void registerDAStructure(DAStructure structure) {
        this.structureList.add(structure);
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
            daPress.usePress();
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
