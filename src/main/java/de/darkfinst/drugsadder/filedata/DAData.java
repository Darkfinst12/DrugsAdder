package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DAPlayer;
import de.darkfinst.drugsadder.api.events.DrugsAdderLoadDataEvent;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class DAData {

    public static AtomicInteger dataMutex = new AtomicInteger(0); // WorldData: -1 = Saving, 0 = Free, >= 1 = Loading
    public static FileConfiguration worldData = null; // World Data Cache for consecutive loading of Worlds. Nulled after a data save


    /**
     * This method reads the data.yml file and loads all data from it
     */
    public static void readData() {
        File file = new File(DA.getInstance.getDataFolder(), "data.yml");
        if (file.exists()) {
            long t1 = System.currentTimeMillis();
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            long t2 = System.currentTimeMillis();
            DA.log.infoLog("Loading data.yml: " + (t2 - t1) + "ms");

            // Check if data is the newest version
            String version = data.getString("Version", null);
            if (version != null) {
                /*
                if (!version.equals(DataSave.dataVersion)) {
                    DA.log.log("Data File is being updated...");
                    File worldFile = new File(DA.getInstance.getDataFolder(), "worlddata.yml");
                    new DataUpdater(data, file, worldFile).update(version);
                    data = YamlConfiguration.loadConfiguration(file);
                    DA.log.log("Data Updated to version: " + DataSave.dataVersion);
                }
                 */
            }

            final List<World> worlds = Bukkit.getWorlds();
            if (DAConfig.loadDataAsync) {
                Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, () -> lwDataTask(worlds, true));
            } else {
                lwDataTask(worlds, false);
            }

            // loading Players
            ConfigurationSection section = data.getConfigurationSection("Players");
            if (section != null) {
                for (String uuidString : section.getKeys(false)) {
                    ConfigurationSection playerSection = section.getConfigurationSection(uuidString);
                    if (playerSection == null) {
                        DA.log.errorLog("Error while loading Player: " + uuidString);
                        continue;
                    }
                    UUID uuid = UUID.fromString(uuidString);
                    DAPlayer daPlayer = new DAPlayer(uuid);
                    for (String drug : playerSection.getKeys(false)) {
                        int addiction = playerSection.getInt(drug);
                        daPlayer.addDrug(drug, addiction);
                    }
                    DA.loader.addDaPlayer(daPlayer);
                }
            }

        } else {
            DA.loader.log("No data.yml found. Creating new one.");
        }

    }

    /**
     * This method loads the data for a given world
     *
     * @param uuid    The UUID of the world
     * @param world   The world
     * @param isAsync Whether the method is called asynchronously or not
     */
    public static void loadWorldData(String uuid, World world, boolean isAsync) {
        if (DAData.worldData == null) {
            File file = new File(DA.getInstance.getDataFolder(), "worlddata.yml");
            if (file.exists()) {
                long t1 = System.currentTimeMillis();
                DAData.worldData = YamlConfiguration.loadConfiguration(file);
                long t2 = System.currentTimeMillis();
                if (t2 - t1 > 15000) {
                    // Spigot is _very_ slow at loading inventories from yml. Paper is way faster.
                    // Notify Admin that loading Data took long (its async so not much of a problem)
                    DA.log.log("Bukkit took " + (t2 - t1) / 1000.0 + "s to load Inventories from the World-Data File (in the Background),", isAsync);
                    DA.log.log("consider switching to Paper, or have less items in Structures if it takes a long time for Structures to become available", isAsync);
                } else {
                    DA.log.debugLog("Loading worlddata.yml: " + (t2 - t1) + "ms", isAsync);
                }
            } else {
                return;
            }
        }

        //Load Structures
        if (DAData.worldData.contains("Structures." + uuid)) {
            ConfigurationSection section = DAData.worldData.getConfigurationSection("Structures." + uuid);
            DA.log.log(String.format("Load Structures for World: %s", uuid), isAsync);
            for (String structure : section.getKeys(false)) {
                switch (structure.toLowerCase()) {
                    case "barrels" -> {
                        ConfigurationSection barrels = section.getConfigurationSection("barrels");
                        for (String barrel : barrels.getKeys(false)) {
                            ConfigurationSection barrelSection = barrels.getConfigurationSection(barrel);
                            DAData.loadBarrelData(world, barrelSection, isAsync);
                        }
                    }
                    case "presses" -> {
                        ConfigurationSection presses = section.getConfigurationSection("presses");
                        for (String press : presses.getKeys(false)) {
                            ConfigurationSection pressSection = presses.getConfigurationSection(press);
                            DAData.loadPressData(world, pressSection, isAsync);
                        }
                    }
                    case "tables" -> {
                        ConfigurationSection tables = section.getConfigurationSection("tables");
                        for (String table : tables.getKeys(false)) {
                            ConfigurationSection tableSection = tables.getConfigurationSection(table);
                            DAData.loadTableData(world, tableSection, isAsync);
                        }
                    }
                    case "plants" -> {
                        ConfigurationSection plants = section.getConfigurationSection("plants");
                        for (String plant : plants.getKeys(false)) {
                            ConfigurationSection plantSection = plants.getConfigurationSection(plant);
                            DAData.loadPlantData(world, plantSection, isAsync);
                        }
                    }
                    default -> DA.log.errorLog("Unknown Structure: " + structure);
                }
            }
            DA.log.log(String.format("Loaded Structures for World: %s", uuid), isAsync);
        } else {
            DA.log.log("No Structures found for World: " + world.getName(), isAsync);
        }

        DrugsAdderLoadDataEvent event = new DrugsAdderLoadDataEvent(isAsync, DrugsAdderLoadDataEvent.Type.WORLD, world);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * This method loads the data for the Barrels
     *
     * @param world   The world
     * @param barrel  The barrel config section
     * @param isAsync Whether the method is called asynchronously or not
     */
    private static void loadBarrelData(World world, ConfigurationSection barrel, boolean isAsync) {
        // Block split by ","
        String block = barrel.getString("sign");
        if (block != null) {
            String[] split = block.split(",");
            if (split.length == 3) {
                Block worldBlock = world.getBlockAt(DAUtil.parseInt(split[0]), DAUtil.parseInt(split[1]), DAUtil.parseInt(split[2]));
                if (worldBlock.getBlockData() instanceof WallSign) {
                    try {
                        DABarrel daBarrel = new DABarrel();
                        daBarrel.create(worldBlock, isAsync);

                        ConfigurationSection invSection = barrel.getConfigurationSection("inv");
                        if (invSection != null) {
                            for (String slot : invSection.getKeys(false)) {
                                daBarrel.getInventory().setItem(DAUtil.parseInt(slot), invSection.getItemStack(slot));
                            }
                        }

                    } catch (Exception e) {
                        DA.log.errorLog("Error while loading Barrel: " + barrel.getCurrentPath(), isAsync);
                        DA.log.logException(e, isAsync);
                    }
                }
            }
        }
    }

    /**
     * This method loads the data for the Presses
     *
     * @param world   The world
     * @param press   The press config section
     * @param isAsync Whether the method is called asynchronously or not
     */
    private static void loadPressData(World world, ConfigurationSection press, boolean isAsync) {
        // Block split by ","
        String block = press.getString("sign");
        if (block != null) {
            String[] split = block.split(",");
            if (split.length == 3) {
                Block worldBlock = world.getBlockAt(DAUtil.parseInt(split[0]), DAUtil.parseInt(split[1]), DAUtil.parseInt(split[2]));
                if (worldBlock.getBlockData() instanceof WallSign) {
                    try {
                        DAPress daPress = new DAPress();
                        daPress.create(worldBlock, isAsync);

                        ConfigurationSection invSection = press.getConfigurationSection("inv");
                        if (invSection != null) {
                            for (String slot : invSection.getKeys(false)) {
                                daPress.addCompressedItem(invSection.getItemStack(slot));
                            }
                        }

                    } catch (Exception e) {
                        DA.log.errorLog("Error while loading Press: " + press.getCurrentPath(), isAsync);
                        DA.log.logException(e, isAsync);
                    }
                }
            }
        }
    }

    /**
     * This method loads the data for the Tables
     *
     * @param world   The world
     * @param table   The table config section
     * @param isAsync Whether the method is called asynchronously or not
     */
    private static void loadTableData(World world, ConfigurationSection table, boolean isAsync) {
        // Block split by ","
        String block = table.getString("sign");
        if (block != null) {
            String[] split = block.split(",");
            if (split.length == 3) {
                Block worldBlock = world.getBlockAt(DAUtil.parseInt(split[0]), DAUtil.parseInt(split[1]), DAUtil.parseInt(split[2]));
                if (worldBlock.getBlockData() instanceof WallSign) {
                    try {
                        DATable daTable = new DATable();
                        daTable.create(worldBlock, isAsync);

                        ConfigurationSection invSection = table.getConfigurationSection("inv");
                        if (invSection != null) {
                            for (String slot : invSection.getKeys(false)) {
                                daTable.getInventory().setItem(DAUtil.parseInt(slot), invSection.getItemStack(slot));
                            }
                        }

                    } catch (Exception e) {
                        DA.log.errorLog("Error while loading Table: " + table.getCurrentPath(), isAsync);
                        DA.log.logException(e, isAsync);
                    }
                }
            }
        }
    }

    /**
     * This method loads the data for the Plants
     *
     * @param world   The world
     * @param table   The plant config section
     * @param isAsync Whether the method is called asynchronously or not
     */
    private static void loadPlantData(World world, ConfigurationSection table, boolean isAsync) {
        // Block split by ","
        String block = table.getString("plant");
        if (block != null) {
            String[] split = block.split(",");
            if (split.length == 3) {
                Block worldBlock = world.getBlockAt(DAUtil.parseInt(split[0]), DAUtil.parseInt(split[1]), DAUtil.parseInt(split[2]));
                DAPlantItem seedItem = DAConfig.seedReader.getSeed(table.getString("seed", "NULL"));
                if (seedItem != null) {
                    try {
                        DAPlant daPlant = new DAPlant(seedItem, seedItem.isCrop(), seedItem.isDestroyOnHarvest(), seedItem.getGrowTime(), seedItem.getDrops());
                        daPlant.create(worldBlock, isAsync);

                    } catch (Exception e) {
                        DA.log.errorLog("Error while loading Plant: " + table.getCurrentPath(), isAsync);
                        DA.log.logException(e, isAsync);
                    }
                }
            }
        }
    }

    /**
     * This method executes the load for the worlds
     *
     * @param worlds  The worlds to load
     * @param isAsync Whether the method is called asynchronously or not
     */
    public static void lwDataTask(List<World> worlds, boolean isAsync) {
        if (!acquireDataLoadMutex(isAsync)) return; // Tries for 60 sec

        try {
            for (World world : worlds) {
                loadWorldData(world.getUID().toString(), world, isAsync);
            }
        } catch (Exception e) {
            DA.log.logException(e, isAsync);
        } finally {
            releaseDataLoadMutex();
            DrugsAdderLoadDataEvent event = new DrugsAdderLoadDataEvent(isAsync, DrugsAdderLoadDataEvent.Type.GLOBAL, null);
            Bukkit.getPluginManager().callEvent(event);
            if (DAConfig.loadDataAsync && DAData.dataMutex.get() == 0) {
                DA.log.log("Background data loading complete.", isAsync);
            }
        }
    }

    public static boolean acquireDataLoadMutex(boolean isAsync) {
        int wait = 0;
        // Increment the Data Mutex if it is not -1
        while (DAData.dataMutex.updateAndGet(i -> i >= 0 ? i + 1 : i) <= 0) {
            wait++;
            if (!DAConfig.loadDataAsync || wait > 60) {
                DA.log.errorLog("Could not load World Data, Mutex: " + DAData.dataMutex.get(), isAsync);
                return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    public static void releaseDataLoadMutex() {
        dataMutex.decrementAndGet();
    }
}
