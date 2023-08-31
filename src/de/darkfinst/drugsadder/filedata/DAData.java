package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderLoadDataEvent;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DAData {

    public static AtomicInteger dataMutex = new AtomicInteger(0); // WorldData: -1 = Saving, 0 = Free, >= 1 = Loading
    public static FileConfiguration worldData = null; // World Data Cache for consecutive loading of Worlds. Nulled after a data save


    public static void readData() {
        File file = new File(DA.getInstance.getDataFolder(), "data.yml");
        if (file.exists()) {
            long t1 = System.currentTimeMillis();
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            long t2 = System.currentTimeMillis();
            DA.log.debugLog("Loading data.yml: " + (t2 - t1) + "ms");

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


        } else {
            DA.loader.log("No data.yml found. Creating new one.");
        }

    }

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
            int barrelsCount = 0;
            int pressesCount = 0;
            int tablesCount = 0;
            for (String structure : section.getKeys(false)) {
                switch (structure.toLowerCase()) {
                    case "barrels" -> {
                        ConfigurationSection barrels = section.getConfigurationSection("barrels");
                        for (String barrel : barrels.getKeys(false)) {
                            ConfigurationSection barrelSection = barrels.getConfigurationSection(barrel);
                            DAData.loadBarrelData(world, barrelSection, isAsync);
                        }
                        barrelsCount++;
                    }
                    case "presses" -> {
                        ConfigurationSection presses = section.getConfigurationSection("presses");
                        for (String press : presses.getKeys(false)) {
                            ConfigurationSection pressSection = presses.getConfigurationSection(press);
                            DAData.loadPressData(world, pressSection, isAsync);
                        }
                        pressesCount++;
                    }
                    case "tables" -> {
                        ConfigurationSection tables = section.getConfigurationSection("tables");
                        for (String table : tables.getKeys(false)) {
                            ConfigurationSection tableSection = tables.getConfigurationSection(table);
                            DAData.loadTableData(world, tableSection, isAsync);
                        }
                        tablesCount++;
                    }
                    default -> {
                        DA.log.errorLog("Unknown Structure: " + structure);
                    }
                }
            }
            DA.log.log(String.format("Loaded Barrels: %s, Presses: %s, Tables: %s", barrelsCount, pressesCount, tablesCount), isAsync);
        } else {
            DA.log.log("No Structures found for World: " + world.getName(), isAsync);
        }

        DrugsAdderLoadDataEvent event = new DrugsAdderLoadDataEvent(isAsync, DrugsAdderLoadDataEvent.Type.WORLD, world);
        Bukkit.getPluginManager().callEvent(event);
    }

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
                } else {
                    DA.log.errorLog("Block is not a WallSign: " + barrel.getCurrentPath(), isAsync);
                }
            } else {
                DA.log.errorLog("Incomplete Block-Data in data.yml: " + barrel.getCurrentPath(), isAsync);
            }
        } else {
            DA.log.errorLog("Missing Block-Data in data.yml: " + barrel.getCurrentPath(), isAsync);
        }
    }

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
                } else {
                    DA.log.errorLog("Block is not a WallSign: " + press.getCurrentPath(), isAsync);
                }
            } else {
                DA.log.errorLog("Incomplete Block-Data in data.yml: " + press.getCurrentPath(), isAsync);
            }
        } else {
            DA.log.errorLog("Missing Block-Data in data.yml: " + press.getCurrentPath(), isAsync);
        }
    }

    private static void loadTableData(World world, ConfigurationSection table, boolean isAsync) {
        // Block split by ","
        String block = table.getString("sign");
        DA.log.debugLog("Loading Table: " + table.getCurrentPath(), isAsync);
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
                } else {
                    DA.log.errorLog("Block is not a WallSign: " + table.getCurrentPath(), isAsync);
                }
            } else {
                DA.log.errorLog("Incomplete Block-Data in data.yml: " + table.getCurrentPath(), isAsync);
            }
        } else {
            DA.log.errorLog("Missing Block-Data in data.yml: " + table.getCurrentPath(), isAsync);
        }
    }

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
