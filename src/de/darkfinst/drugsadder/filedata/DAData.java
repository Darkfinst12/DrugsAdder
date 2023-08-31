package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.util.List;
import java.util.UUID;
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
                Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, () -> lwDataTask(worlds));
            } else {
                lwDataTask(worlds);
            }


        } else {
            DA.loader.log("No data.yml found. Creating new one.");
        }

    }

    public static void loadWorldData(String uuid, World world) {

    }

    public static void lwDataTask(List<World> worlds) {
        if (!acquireDataLoadMutex()) return; // Tries for 60 sec

        try {
            for (World world : worlds) {
                loadWorldData(world.getUID().toString(), world);
            }
        } catch (Exception e) {
            DA.log.logException(e);
        } finally {
            releaseDataLoadMutex();
            if (DAConfig.loadDataAsync && DAData.dataMutex.get() == 0) {
                DA.log.log("Background data loading complete.");
            }
        }
    }

    public static boolean acquireDataLoadMutex() {
        int wait = 0;
        // Increment the Data Mutex if it is not -1
        while (DAData.dataMutex.updateAndGet(i -> i >= 0 ? i + 1 : i) <= 0) {
            wait++;
            if (!DAConfig.loadDataAsync || wait > 60) {
                DA.log.errorLog("Could not load World Data, Mutex: " + DAData.dataMutex.get());
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
