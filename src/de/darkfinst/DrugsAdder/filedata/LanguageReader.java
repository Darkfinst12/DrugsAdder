package de.darkfinst.DrugsAdder.filedata;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.DALoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageReader {

    private final DALoader daLoader;
    private final File file;
    private FileConfiguration config;

    public LanguageReader() {
        this.daLoader = DA.loader;
        this.file = new File(DA.getInstance.getDataFolder(), "config.yml");
        try {
            this.config = YamlConfiguration.loadConfiguration(this.file);
        } catch (Exception ignored) {
        }


    }


}
