package de.darkfinst.drugsadder.filedata.data;

import de.darkfinst.drugsadder.DA;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class DataUpdater {

    private FileConfiguration data;
    private File file;
    private File worldFile;

    public DataUpdater(FileConfiguration data, File file, File worldFile) {
        this.data = data;
        this.file = file;
        this.worldFile = worldFile;
    }


    public void update(String fromVersion) {
        //If there is ever a new version, this method will be used to update the data.yml file
        //Just call the corresponding update method for the version you want to update from
        try {
            data.save(file);
        } catch (IOException e) {
            DA.log.logException(e);
        }
    }
}
