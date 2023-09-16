package de.darkfinst.drugsadder.filedata.data;

import de.darkfinst.drugsadder.DA;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class ReadOldData extends BukkitRunnable {

    @Getter
    public FileConfiguration data;
    public boolean done = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void run() {
        int wait = 0;
        // Set the Data Mutex to -1 if it is 0=Free
        while (!DAData.dataMutex.compareAndSet(0, -1)) {
            if (wait > 300) {
                DA.log.errorLog("Loading Process active for too long while trying to save! Mutex: " + DAData.dataMutex.get());
                return;
            }
            wait++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }


        File worldDataFile = new File(DA.getInstance.getDataFolder(), "worlddata.yml");
        if (DAData.worldData == null) {
            if (!worldDataFile.exists()) {
                data = new YamlConfiguration();
                done = true;
                return;
            }

            data = YamlConfiguration.loadConfiguration(worldDataFile);
        } else {
            data = DAData.worldData;
        }

        if (DataSave.lastBackup > 10) {
            worldDataFile.renameTo(new File(DA.getInstance.getDataFolder(), "worlddataBackup.yml"));
            DataSave.lastBackup = 0;
        } else {
            DataSave.lastBackup++;
        }

        done = true;
    }

}
