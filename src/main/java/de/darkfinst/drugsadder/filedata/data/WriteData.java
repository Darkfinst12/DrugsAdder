package de.darkfinst.drugsadder.filedata.data;

import de.darkfinst.drugsadder.DA;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class WriteData implements Runnable {

    /**
     * The data to save
     */
    private FileConfiguration data;
    /**
     * The world data to save
     */
    private FileConfiguration worldData;

    public WriteData(FileConfiguration data, FileConfiguration worldData) {
        this.data = data;
        this.worldData = worldData;
    }

    /**
     * Saves the data to the data file
     */
    @Override
    public void run() {
        File datafile = new File(DA.getInstance.getDataFolder(), "data.yml");
        File worlddatafile = new File(DA.getInstance.getDataFolder(), "worlddata.yml");

        try {
            data.save(datafile);
        } catch (Exception e) {
            DA.log.logException(e);
        }
        try {
            worldData.save(worlddatafile);
        } catch (Exception e) {
            DA.log.logException(e);
        }

        DataSave.lastSave = 1;
        DataSave.running = null;
        DAData.dataMutex.set(0);
    }
}
