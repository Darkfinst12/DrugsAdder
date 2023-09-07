package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.api.events.RegisterStructureEvent;
import de.darkfinst.drugsadder.filedata.DAData;
import de.darkfinst.drugsadder.filedata.DataSave;
import de.darkfinst.drugsadder.filedata.LanguageReader;
import de.darkfinst.drugsadder.listeners.*;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class DALoader {

    private final DA plugin;

    public LanguageReader languageReader;
    public String language;

    private final ArrayList<DAStructure> structureList = new ArrayList<>();
    private final ArrayList<DAPlayer> daPlayerList = new ArrayList<>();

    @Setter
    public static boolean iaLoaded = false;

    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    public void init() {
        if (DAConfig.hasItemsAdder && !DALoader.iaLoaded) {
            this.initConfig();
            this.initData();
        } else {
            this.infoLog("ItemsAdder is not loaded, await finishing of the ItemsAdder loading process");
        }
        this.initCommands();
        this.initListener();
        this.initRunnable();
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
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }

    private void initData() {
        try {
            DAData.readData();
        } catch (Exception e) {
            this.logException(e);
        }
    }

    private void initCommands() {
        new DACommand().register();
    }

    private void initListener() {
        new BlockBreakEventListener();
        new BlockGrowEventListener();
        new BlockPlaceEventListener();
        new CraftItemEventListener();
        new FurnaceBurnEventListener();
        new FurnaceSmeltEventListener();
        new FurnaceStartSmeltEventListener();
        new InventoryClickEventListener();
        new InventoryCloseEventListener();
        new InventoryDragEventListener();
        new ItemsAdderLoadDataEventListener();
        new PlayerInteractEventListener();
        new PlayerItemConsumeEventListener();
        new PrepareItemCraftEventListener();
        new SignChangeEventListener();
        new StructureGrowEventListener();
    }

    private void initRunnable() {
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new DrugsAdderRunnable(), 650, 1200);
    }

    public boolean registerDAStructure(DAStructure structure, boolean isAsync) {
        RegisterStructureEvent registerStructureEvent = new RegisterStructureEvent(isAsync, structure);
        this.plugin.getServer().getPluginManager().callEvent(registerStructureEvent);
        if (!registerStructureEvent.isCancelled()) {
            return this.structureList.add(structure);
        }
        return false;
    }

    public void unregisterDAStructure(DAStructure structure) {
        this.structureList.remove(structure);
    }

    public boolean unregisterDAStructure(Player player, Block block) {
        DAStructure structure = this.getStructure(block);
        if (structure != null) {
            if (structure.hasInventory()) {
                structure.destroyInventory();
            }
            boolean success = this.structureList.remove(structure);
            if (success) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Structure_Destroyed", structure.getClass().getSimpleName()), DrugsAdderSendMessageEvent.Type.PLAYER);
            }

        }
        return this.structureList.contains(structure);
    }

    public boolean isStructure(Block block) {
        return this.structureList.stream().anyMatch(daStructure -> daStructure.isBodyPart(block));
    }

    public boolean isPlant(Block block) {
        return this.structureList.stream().anyMatch(daStructure -> daStructure instanceof DAPlant daPlant && daPlant.isBodyPart(block));
    }

    public DAStructure getStructure(Block block) {
        return this.structureList.stream().filter(daStructure -> daStructure.isBodyPart(block)).findAny().orElse(null);
    }

    public DAStructure getStructure(Inventory inventory) {
        return this.structureList.stream().filter(daStructure -> {
            if (daStructure instanceof DABarrel daBarrel) {
                return daBarrel.getInventory().equals(inventory);
            } else if (daStructure instanceof DATable daTable) {
                return daTable.getInventory().equals(inventory);
            }
            return false;
        }).findAny().orElse(null);
    }

    public List<DAStructure> getStructures(World world) {
        return this.structureList.stream().filter(daStructure -> daStructure.getWorld().equals(world)).toList();
    }

    public void unloadStructures(World world) {
        this.structureList.removeIf(daStructure -> daStructure.getWorld().equals(world));
    }

    public void openStructure(Block block, Player player) {
        DAStructure daStructure = this.getStructure(block);
        if (daStructure instanceof DABarrel daBarrel) {
            daBarrel.open(player, block);
        } else if (daStructure instanceof DATable daTable) {
            daTable.open(player);
        } else if (daStructure instanceof DAPress daPress) {
            daPress.usePress(player, block);
        } else if (daStructure instanceof DAPlant daPlant) {
            daPlant.checkHarvest(player);
        }
    }


    //Logging
    public void msg(CommandSender sender, String msg) {
        this.msg(sender, msg, DrugsAdderSendMessageEvent.Type.NONE);
    }

    public void msg(CommandSender sender, String msg, DrugsAdderSendMessageEvent.Type Type) {
        this.msg(sender, msg, Type, false);
    }

    public void msg(CommandSender sender, String msg, DrugsAdderSendMessageEvent.Type Type, boolean isAsync) {
        DrugsAdderSendMessageEvent sendMessageEvent = new DrugsAdderSendMessageEvent(isAsync, sender, msg, Type);
        this.plugin.getServer().getPluginManager().callEvent(sendMessageEvent);
        if (!sendMessageEvent.isCancelled()) {
            sender.sendMessage(ChatColor.of(new Color(3, 94, 212)) + "[DrugsAdder] " + ChatColor.WHITE + sendMessageEvent.getMessage());
        }
    }

    public void log(String msg) {
        this.log(msg, false);
    }

    public void log(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.LOG, isAsync);
    }

    public void infoLog(String msg) {
        this.infoLog(msg, false);
    }

    public void infoLog(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(41, 212, 3)) + "[Info] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.INFO, isAsync);
    }

    public void debugLog(String msg) {
        this.debugLog(msg, false);
    }

    public void debugLog(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(212, 192, 3)) + "[Debug] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.DEBUG, isAsync);
    }

    public void errorLog(String msg) {
        this.errorLog(msg, false);
    }

    public void errorLog(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(196, 33, 33)) + "[ERROR] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.ERROR, isAsync);
    }

    public void logException(Exception e) {
        this.logException(e, false);
    }

    public void logException(Exception e, boolean isAsync) {
        String s = e.getMessage() == null ? "null" : e.getMessage();
        StringBuilder log = new StringBuilder(s);
        Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> log.append("\n       ").append(stackTraceElement.toString()));
        this.errorLog(log.toString(), isAsync);
    }

    public void reloadConfigIA() {
        this.clearConfigData();
        this.initConfig();
        this.initData();
    }

    public void reloadConfig() {
        this.clearConfigData();
        this.initConfig();
    }

    private void clearConfigData() {
        DAConfig.clear();
    }


    public void unload() {
        DataSave.save(true);
    }

    public DAPlayer getDaPlayer(Player player) {
        return this.daPlayerList.stream().filter(daPlayer -> daPlayer.getUuid().equals(player.getUniqueId())).findAny().orElse(null);
    }

    public void removeDaPlayer(DAPlayer daPlayer) {
        this.daPlayerList.remove(daPlayer);
    }

    public void addDaPlayer(DAPlayer daPlayer) {
        this.daPlayerList.add(daPlayer);
    }

    public String getTranslation(String fallback, String key, String... args) {
        if (this.languageReader == null) {
            return fallback;
        }
        return this.languageReader.get(key, args);
    }

    public class DrugsAdderRunnable implements Runnable {
        @Override
        public void run() {
            long t1 = System.nanoTime();
            DataSave.autoSave();
            long t2 = System.nanoTime();
            debugLog("DrugsAdderRunnable: t1: " + TimeUnit.NANOSECONDS.toMillis(t2 - t1) + " ms");
        }
    }

}
