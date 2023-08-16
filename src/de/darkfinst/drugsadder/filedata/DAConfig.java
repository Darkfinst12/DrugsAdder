package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.utils.DAUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DAConfig {

    private static final String configVersion = "0.1";

    public static DALoader loader = DA.loader;

    public static boolean checkConfig() {
        File file = new File(DA.getInstance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            DA.loader.errorLog(ChatColor.BOLD + "No config.yml found, creating default file! You may want to choose a config according to your language!");
            DA.loader.log(ChatColor.BOLD + "You can find them in plugins/DrugsAdder/configs/");
            DA.loader.log(ChatColor.BOLD + "Just copy the config for your language into the DrugsAdder folder and use /drugsadder reload");
            InputStream defConf = DA.getInstance.getResource("config/en/config.yml");
            if (defConf == null) {
                DA.loader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
                return false;
            }
            try {
                DAUtil.saveFile(defConf, DA.getInstance.getDataFolder(), "config.yml", false);
            } catch (IOException e) {
                DA.loader.errorLog(e.getMessage());
                for (StackTraceElement element : e.getStackTrace()) {
                    DA.loader.log(element.toString());
                }
                return false;
            }
        }
        if (!file.exists()) {
            DA.loader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
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

    public static FileConfiguration loadConfigFile() {
        FileConfiguration fileConfiguration = null;
        File file = new File(DA.getInstance.getDataFolder(), "config.yml");
        if (checkConfig()) {
            try {
                FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                if (cfg.contains("version") && cfg.contains("language")) {
                    fileConfiguration = cfg;
                }
            } catch (Exception e) {
                // Failed to load
                if (loader.languageReader != null) {
                    loader.errorLog(loader.languageReader.get("Error_YmlRead"));
                    Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> loader.log(stackTraceElement.toString()));
                } else {
                    loader.errorLog("Could not read file config.yml, please make sure the file is in valid yml format (correct spaces etc.)");
                }
            }
        }
        return fileConfiguration;
    }

    public static void readConfig(FileConfiguration config) {

        //Set Language
        loader.language = config.getString("language", "en");

        //Loads the LanguageReader
        loader.languageReader = new LanguageReader(new File(DA.getInstance.getDataFolder(), "languages/" + loader.language + ".yml"), "languages/" + loader.language + ".yml");

        // Check if config is the newest version
        String version = config.getString("version", null);
        if (version != null) {
            if (!version.equals(configVersion)) {
                File file = new File(DA.getInstance.getDataFolder(), "config.yml");
                copyDefaultConfigs(true);
                config = YamlConfiguration.loadConfiguration(file);
            }
        }

    }

}
