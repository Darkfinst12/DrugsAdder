package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.RegisterStructureEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.filedata.data.DAData;
import de.darkfinst.drugsadder.filedata.data.DataSave;
import de.darkfinst.drugsadder.filedata.readers.LanguageReader;
import de.darkfinst.drugsadder.listeners.*;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class DALoader {

    /**
     * Whether the ItemsAdder plugin is loaded or not
     * <br>
     * Can be ignored if the ItemsAdder plugin is not used
     */
    @Setter
    public static boolean iaLoaded = false;
    /**
     * The prefix, which is used for all messages
     */
    public final String prefix = ChatColor.of(new Color(3, 94, 212)) + "[DrugsAdder] " + ChatColor.WHITE;
    /**
     * The instance of the plugin
     */
    private final DA plugin;
    /**
     * The list of all registered structures
     */
    private final ArrayList<DAStructure> structureList = new ArrayList<>();
    /**
     * The list of all registered players
     */
    private final ArrayList<DAPlayer> daPlayerList = new ArrayList<>();
    /**
     * The language reader for the language files
     */
    public LanguageReader languageReader;
    /**
     * The language, which is used for the language files
     */
    public String language;

    public DALoader(DA plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the plugin and all its components
     */
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

    /**
     * Initializes the config
     */
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

    /**
     * Initializes the data
     */
    private void initData() {
        try {
            DAData.readData();
        } catch (Exception e) {
            this.logException(e);
        }
    }

    /**
     * Initializes the commands
     */
    private void initCommands() {
        new DACommand().register();
    }

    /**
     * Initializes the listeners
     */
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

    /**
     * Initializes the runnable
     */
    private void initRunnable() {
        this.plugin.getServer().getScheduler().runTaskTimer(this.plugin, new DrugsAdderRunnable(), 650, 1200);
    }

    //Structures

    /**
     * Registers a structure
     *
     * @param structure The structure to register
     * @param isAsync   Whether the event should be called async or not
     * @return Whether the structure was registered or not
     * @throws RegisterStructureException If the structure is already registered
     */
    public boolean registerDAStructure(DAStructure structure, boolean isAsync) throws RegisterStructureException {
        RegisterStructureEvent registerStructureEvent = new RegisterStructureEvent(isAsync, structure);
        this.plugin.getServer().getPluginManager().callEvent(registerStructureEvent);
        if (!registerStructureEvent.isCancelled()) {
            DAStructure oldStructure = this.structureList.stream().filter(daStructure -> daStructure.isSimilar(structure)).findFirst().orElse(null);
            if (oldStructure != null && oldStructure.isForRemoval()) {
                oldStructure.setForRemoval(false);
            } else if (oldStructure != null) {
                throw new RegisterStructureException("Structure already registered");
            } else {
                this.structureList.add(structure);
                return this.getStructure(structure) != null;
            }
        }
        return false;
    }

    /**
     * Unregisters a structure
     *
     * @param structure The structure to unregister
     * @return Whether the structure was unregistered or not
     */
    public boolean unregisterDAStructure(DAStructure structure) {
        structure.setForRemoval(true);
        return this.getStructure(structure) == null;
    }

    /**
     * Unregisters a structure
     *
     * @param player The player, which wants to unregister the structure
     * @param block  The block, which is part of the structure
     * @return Whether the structure was unregistered or not
     */
    public boolean unregisterDAStructure(Player player, Block block) {
        DAStructure structure = this.getStructure(block);
        if (structure != null) {
            if (structure.hasInventory()) {
                structure.destroyInventory();
            }
            boolean success = this.unregisterDAStructure(structure);
            if (success) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Structure_Destroyed", structure.getClass().getSimpleName().replace("DA", "")), DrugsAdderSendMessageEvent.Type.PLAYER);
            }

        }
        return this.structureList.contains(structure);
    }

    /**
     * Checks if the block is part of a structure
     *
     * @param block The block to check
     * @return Whether the block is part of a structure or not
     */
    public boolean isStructure(Block block) {
        return this.structureList.stream().anyMatch(daStructure -> daStructure.isBodyPart(block) && !daStructure.isForRemoval());
    }

    /**
     * Checks if the block is part of a plant
     *
     * @param block The block to check
     * @return Whether the block is part of a plant or not
     */
    public boolean isPlant(Block block) {
        return this.structureList.stream().anyMatch(daStructure -> daStructure instanceof DAPlant daPlant && daPlant.isBodyPart(block) && !daStructure.isForRemoval());
    }

    /**
     * Get a structure by a block
     *
     * @param block The block, which is part of the structure
     * @return The structure, which contains the block or null if no structure was found
     */
    public DAStructure getStructure(Block block) {
        return this.structureList.stream().filter(daStructure -> !daStructure.isForRemoval() && daStructure.isBodyPart(block)).findAny().orElse(null);
    }

    public DAStructure getStructure(DAStructure structure) {
        return this.structureList.stream().filter(daStructure -> !daStructure.isForRemoval() && daStructure.equals(structure)).findAny().orElse(null);
    }

    /**
     * Get a structure by an inventory of the structure
     * <br>
     * Works only for structures, which have an inventory like barrels or tables otherwise null will always be returned
     *
     * @param inventory The inventory of the structure
     * @return The structure, which contains the inventory or null if no structure was found
     */
    public DAStructure getStructure(Inventory inventory) {
        return this.structureList.stream().filter(daStructure -> {
            if (daStructure instanceof DABarrel daBarrel) {
                return Objects.equals(daBarrel.getInventory(), inventory);
            } else if (daStructure instanceof DATable daTable) {
                return Objects.equals(daTable.getInventory(), inventory);
            } else if (daStructure instanceof DACrafter daCrafter) {
                return Objects.equals(daCrafter.getInventory(), inventory);
            }
            return false;
        }).filter(daStructure -> !daStructure.isForRemoval()).findAny().orElse(null);
    }

    public List<DAStructure> getStructures(World world) {
        return this.structureList.stream().filter(daStructure -> daStructure.getWorld().equals(world) && !daStructure.isForRemoval()).toList();
    }

    /**
     * Unloads all structures of a world
     *
     * @param world The world, which structures should be unloaded
     */
    public void unloadStructures(World world) {
        this.structureList.removeIf(daStructure -> daStructure.getWorld().equals(world));
    }

    /**
     * Opens a structure for a player
     *
     * @param block  The block, which is part of the structure
     * @param player The player, which opens the structure
     */
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
        } else if (daStructure instanceof DACrafter daCRafter) {
            daCRafter.open(player);
        }
    }

    //DAPlayer

    /**
     * Get a DAPlayer by a player
     *
     * @param player The player, which is part of the DAPlayer
     * @return The DAPlayer, which contains the player or null if no DAPlayer was found
     */
    public DAPlayer getDaPlayer(Player player) {
        return this.daPlayerList.stream().filter(daPlayer -> daPlayer.getUuid().equals(player.getUniqueId())).findAny().orElse(null);
    }

    public void removeDaPlayer(DAPlayer daPlayer) {
        this.daPlayerList.remove(daPlayer);
    }

    /**
     * Adds a DAPlayer to the list of DAPlayers
     *
     * @param daPlayer The DAPlayer to add
     */
    public void addDaPlayer(DAPlayer daPlayer) {
        this.daPlayerList.add(daPlayer);
    }

    //Language

    /**
     * Gets a translation from the language files
     *
     * @param fallback The fallback, if the language reader is null
     * @param key      The key of the translation
     * @param args     The arguments for the translation
     * @return The translation or the fallback if the language reader is null
     */
    public String getTranslation(String fallback, String key, String... args) {
        if (this.languageReader == null) {
            return fallback;
        }
        return this.languageReader.get(key, args);
    }


    //Config
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

    /**
     * What should be done when the plugin is unloaded
     */
    public void unload() {
        DataSave.save(true);
    }


    //Logging and Messaging

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type)} wit the type {@link DrugsAdderSendMessageEvent.Type#NONE}
     *
     * @param sender The CommandSender to send the message to
     * @param msg    The message to send
     */
    public void msg(CommandSender sender, String msg) {
        this.msg(sender, msg, DrugsAdderSendMessageEvent.Type.NONE);
    }

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type, boolean)} and sets isAsync to false
     *
     * @param sender The CommandSender to send the message to
     * @param msg    The message to send
     * @param Type   The type of the message
     */
    public void msg(CommandSender sender, String msg, DrugsAdderSendMessageEvent.Type Type) {
        this.msg(sender, msg, Type, false);
    }

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It calls the {@link DrugsAdderSendMessageEvent} and sends the message to the CommandSender if the event is not canceled
     *
     * @param sender  The CommandSender to send the message to
     * @param msg     The message to send
     * @param type    The type of the message
     * @param isAsync Whether the event should be called async or not
     */
    public void msg(CommandSender sender, String msg, DrugsAdderSendMessageEvent.Type type, boolean isAsync) {
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        DrugsAdderSendMessageEvent sendMessageEvent = new DrugsAdderSendMessageEvent(isAsync, sender, msg, type);
        this.plugin.getServer().getPluginManager().callEvent(sendMessageEvent);
        if (!sendMessageEvent.isCancelled()) {
            sender.sendMessage(this.prefix + sendMessageEvent.getMessage());
        }
    }

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It executes {@link DALoader#msg(CommandSender, BaseComponent, DrugsAdderSendMessageEvent.Type)} wit the type {@link DrugsAdderSendMessageEvent.Type#NONE}
     *
     * @param sender The CommandSender to send the message to
     * @param msg    The message to send
     */
    public void msg(CommandSender sender, BaseComponent msg) {
        this.msg(sender, msg, DrugsAdderSendMessageEvent.Type.NONE);
    }

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It executes {@link DALoader#msg(CommandSender, BaseComponent, DrugsAdderSendMessageEvent.Type, boolean)} and sets isAsync to false
     *
     * @param sender The CommandSender to send the message to
     * @param msg    The message to send
     * @param Type   The type of the message
     */
    public void msg(CommandSender sender, BaseComponent msg, DrugsAdderSendMessageEvent.Type Type) {
        this.msg(sender, msg, Type, false);
    }

    /**
     * Sends a message to a CommandSender
     * <br>
     * CommandSender can be a Player or the Console
     * <p>
     * It calls the {@link DrugsAdderSendMessageEvent} and sends the message to the CommandSender if the event is not canceled
     *
     * @param sender  The CommandSender to send the message to
     * @param msg     The message to send
     * @param type    The type of the message
     * @param isAsync Whether the event should be called async or not
     */
    public void msg(CommandSender sender, BaseComponent msg, DrugsAdderSendMessageEvent.Type type, boolean isAsync) {
        DrugsAdderSendMessageEvent sendMessageEvent = new DrugsAdderSendMessageEvent(isAsync, sender, msg, type);
        this.plugin.getServer().getPluginManager().callEvent(sendMessageEvent);
        if (!sendMessageEvent.isCancelled()) {
            assert sendMessageEvent.getComponent() != null;
            sender.spigot().sendMessage(sendMessageEvent.getComponent());
        }
    }

    /**
     * Logs a message to the console
     * <p>
     * It executes {@link DALoader#log(String, boolean)} and sets isAsync to false
     *
     * @param msg The message to log
     */
    public void log(String msg) {
        this.log(msg, false);
    }

    /**
     * Logs a messsage to the console
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type, boolean)} and sets the sender to the console and the type to {@link DrugsAdderSendMessageEvent.Type#LOG}
     *
     * @param msg     The message to log
     * @param isAsync Whether the event should be called async or not
     */
    public void log(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.LOG, isAsync);
    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [Info]
     * <p>
     * It executes {@link DALoader#infoLog(String, boolean)} and sets isAsync to false
     *
     * @param msg The message to log
     */
    public void infoLog(String msg) {
        this.infoLog(msg, false);
    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [Info]
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type, boolean)} and sets the sender to the console and the type to {@link DrugsAdderSendMessageEvent.Type#INFO}
     *
     * @param msg     The message to log
     * @param isAsync Whether the event should be called async or not
     */
    public void infoLog(String msg, boolean isAsync) {
        if (DAConfig.logGeneralInfo) {
            this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(41, 212, 3)) + "[Info] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.INFO, isAsync);
        }
    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [Debug]
     * <p>
     * It executes {@link DALoader#debugLog(String, boolean)} and sets isAsync to false
     *
     * @param msg The message to log
     */
    public void debugLog(String msg) {
        this.debugLog(msg, false);
    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [Debug]
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type, boolean)} and sets the sender to the console and the type to {@link DrugsAdderSendMessageEvent.Type#DEBUG}
     *
     * @param msg     The message to log
     * @param isAsync Whether the event should be called async or not
     */
    public void debugLog(String msg, boolean isAsync) {
        if (DAConfig.debugLogg) {
            this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(212, 192, 3)) + "[Debug] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.DEBUG, isAsync);
        }

    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [ERROR]
     * <p>
     * It executes {@link DALoader#errorLog(String, boolean)} and sets isAsync to false
     *
     * @param msg The message to log
     */
    public void errorLog(String msg) {
        this.errorLog(msg, false);
    }

    /**
     * Logs a message to the console
     * <br>
     * The message receives the prefix [ERROR]
     * <p>
     * It executes {@link DALoader#msg(CommandSender, String, DrugsAdderSendMessageEvent.Type, boolean)} and sets the sender to the console and the type to {@link DrugsAdderSendMessageEvent.Type#ERROR}
     *
     * @param msg     The message to log
     * @param isAsync Whether the event should be called async or not
     */
    public void errorLog(String msg, boolean isAsync) {
        this.msg(Bukkit.getConsoleSender(), ChatColor.of(new Color(196, 33, 33)) + "[ERROR] " + ChatColor.WHITE + msg, DrugsAdderSendMessageEvent.Type.ERROR, isAsync);
    }

    /**
     * Logs an exception to the console
     * <p>
     * It executes {@link DALoader#logException(Exception, boolean)} and sets isAsync to false
     *
     * @param e The exception to log
     */
    public void logException(Exception e) {
        this.logException(e, false);
    }

    /**
     * Logs an exception to the console
     * <p>
     * It executes {@link DALoader#errorLog(String, boolean)} and sets the message to the exception message
     *
     * @param e       The exception to log
     * @param isAsync Whether the event should be called async or not
     */
    public void logException(Exception e, boolean isAsync) {
        String s = e.getMessage() == null ? "null" : e.getMessage();
        StringBuilder log = new StringBuilder(s);
        Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> log.append("\n       ").append(stackTraceElement.toString()));
        this.errorLog(log.toString(), isAsync);
    }

    //Runnable

    /**
     * The HartBeat runnable, which saves the data
     */
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
