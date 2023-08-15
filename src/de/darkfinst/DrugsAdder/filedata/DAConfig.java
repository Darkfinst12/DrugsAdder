package de.darkfinst.DrugsAdder.filedata;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.DALoader;
import de.darkfinst.DrugsAdder.utils.DAUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class DAConfig {

    private static final String configVersion = "0.1";

    private final DALoader daLoader;
    private final File file;
    private FileConfiguration config;

    public DAConfig() {
        this.daLoader = DA.loader;
        this.file = new File(DA.getInstance.getDataFolder(), "config.yml");
        try {
            this.config = YamlConfiguration.loadConfiguration(this.file);
        } catch (Exception ignored) {
        }


    }

    public boolean checkConfig() {
        if (!this.file.exists()) {
            this.daLoader.errorLog(ChatColor.BOLD + "No config.yml found, creating default file! You may want to choose a config according to your language!");
            this.daLoader.log(ChatColor.BOLD + "You can find them in plugins/DrugsAdder/configs/");
            this.daLoader.log(ChatColor.BOLD + "Just copy the config for your language into the DrugsAdder folder and use /drugsadder reload");
            InputStream defConf = DA.getInstance.getResource("config/en/config.yml");
            if (defConf == null) {
                this.daLoader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
                return false;
            }
            try {
                DAUtil.saveFile(defConf, DA.getInstance.getDataFolder(), "config.yml", false);
            } catch (IOException e) {
                this.daLoader.errorLog(e.getMessage());
                for (StackTraceElement element : e.getStackTrace()) {
                    this.daLoader.log(element.toString());
                }
                return false;
            }
        }
        if (!this.file.exists()) {
            this.daLoader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
            return false;
        }
        try {
            this.config = YamlConfiguration.loadConfiguration(this.file);
        } catch (Exception e) {
            this.daLoader.errorLog(e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                this.daLoader.log(element.toString());
            }
            return false;
        }


        copyDefaultConfigs(false);
        return true;
    }

    private static void copyDefaultConfigs(boolean overwrite) {
        File configs = new File(DA.getInstance.getDataFolder(), "configs");
        File languages = new File(DA.getInstance.getDataFolder(), "languages");
        for (String l : new String[]{"de", "en"}) {
            File lfold = new File(configs, l);
            try {
                DAUtil.saveFile(DA.getInstance.getResource("config/" + l + "/config.yml"), lfold, "config.yml", overwrite);
                DAUtil.saveFile(DA.getInstance.getResource("languages/" + l + ".yml"), languages, l + ".yml", false); // Never overwrite languages, they get updated with their updater
            } catch (IOException ignored) {
            }
        }
    }

    public void readConfig() {

    }

}
