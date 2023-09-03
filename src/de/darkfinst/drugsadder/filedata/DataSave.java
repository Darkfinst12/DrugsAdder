package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DAPlayer;
import de.darkfinst.drugsadder.structures.DAStructure;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class DataSave extends BukkitRunnable {

    public static int lastBackup = 0;
    public static int lastSave = 1;
    public static int autosave = 3;
    final public static String dataVersion = "0.0.1";
    public static DataSave running;
    public static List<World> unloadingWorlds = new CopyOnWriteArrayList<>();

    public ReadOldData read;
    private final long time;
    private final List<World> loadedWorlds;
    public boolean collected = false;

    // Not Thread-Safe! Needs to be run in main thread but uses async Read/Write
    public DataSave(ReadOldData read) {
        this.read = read;
        time = System.currentTimeMillis();
        loadedWorlds = Bukkit.getWorlds();
    }


    // Running in Main Thread
    @Override
    public void run() {
        try {
            long saveTime = System.nanoTime();
            // Mutex has been acquired in ReadOldData
            FileConfiguration oldWorldData;
            if (read != null) {
                if (!read.done) {
                    // Wait for async thread to load old data
                    if (System.currentTimeMillis() - time > 50000) {
                        DA.log.errorLog("Old Data took too long to load! Mutex: " + DAData.dataMutex.get());
                        try {
                            cancel();
                            read.cancel();
                        } catch (IllegalStateException ignored) {
                        }
                        running = null;
                        DAData.dataMutex.set(0);
                    }
                    return;
                }
                oldWorldData = read.getData();
            } else {
                oldWorldData = new YamlConfiguration();
            }
            try {
                cancel();
            } catch (IllegalStateException ignored) {
            }
            DAData.worldData = null;

            FileConfiguration data = new YamlConfiguration();
            FileConfiguration worldData = new YamlConfiguration();

            //Start Save of Structures
            DAStructure.save(worldData.createSection("Structures"), oldWorldData.getConfigurationSection("Structures"));
            //End Save Of Structures

            //Start Save of Players
            if (!DA.loader.getDaPlayerList().isEmpty()) {
                DA.log.log("Saving " + DA.loader.getDaPlayerList().size() + " Players");
                DAPlayer.save(data.createSection("Players"));
            } else {
                DA.log.log("No Players to save");
            }
            //End Save of Players

            saveWorldNames(worldData, oldWorldData.getConfigurationSection("Worlds"));

            data.set("Version", dataVersion);

            collected = true;

            if (!unloadingWorlds.isEmpty()) {
                try {
                    for (World world : unloadingWorlds) {
                        // In the very most cases, it is just one world, so just looping like this is fine
                        DA.loader.unloadStructures(world);
                    }
                } catch (Exception e) {
                    DA.log.logException(e);
                }
                unloadingWorlds.clear();
            }

            DA.log.debugLog("Saving: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - saveTime) + "ms");

            if (DA.getInstance.isEnabled()) {
                DA.getInstance.getServer().getScheduler().runTaskAsynchronously(DA.getInstance, new WriteData(data, worldData));
            } else {
                new WriteData(data, worldData).run();
            }
            // Mutex will be released in WriteData
        } catch (Exception e) {
            DA.log.logException(e);
            DAData.dataMutex.set(0);
        }
    }

    public void saveWorldNames(FileConfiguration root, ConfigurationSection old) {
        if (old != null) {
            root.set("Worlds", old);
        }
        for (World world : loadedWorlds) {
            String worldName = world.getUID().toString();
            root.set("Worlds." + worldName, world.getName());
        }
    }

    // Finish the collection of data immediately
    public void now() {
        if (!read.done) {
            read.cancel();
            read.run();
        }
        if (!collected) {
            cancel();
            run();
        }
    }


    // Save all data. Takes a boolean whether all data should be collected in instantly
    public static void save(boolean collectInstant) {
        if (running != null) {
            DA.log.log("Another Save was started while a Save was in Progress");
            if (collectInstant) {
                running.now();
            }
            return;
        }

        ReadOldData read = new ReadOldData();
        if (collectInstant) {
            read.run();
            running = new DataSave(read);
            running.run();
        } else {
            read.runTaskAsynchronously(DA.getInstance);
            running = new DataSave(read);
            running.runTaskTimer(DA.getInstance, 1, 2);
        }
    }

    public static void autoSave() {
        if (lastSave >= autosave) {
            save(false);// save all data
        } else {
            lastSave++;
        }
    }
}
