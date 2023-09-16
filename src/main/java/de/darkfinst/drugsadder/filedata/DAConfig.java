package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.filedata.readers.*;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAConfig {

    private static final String configVersion = "0.1";

    private static final DALoader loader = DA.loader;

    public static boolean loadDataAsync;
    public static boolean returnBucket;
    public static boolean returnBottle;
    public static boolean resetItemCrafting;
    public static boolean resetItemSmelting;

    public static boolean hasSlimefun;
    public static boolean hasItemsAdder;


    public static String cancelRecipeItem;
    public static String suspiciousPotionItem;


    public static DACustomItemReader customItemReader;
    public static boolean logCustomItemLoadInfo;
    public static boolean logCustomItemLoadComplete;
    public static boolean logCustomItemLoadError;

    public static DARecipeReader daRecipeReader;
    public static boolean logRecipeLoadInfo;
    public static boolean logRecipeLoadComplete;
    public static boolean logRecipeLoadError;

    public static Map<Integer, String> tableStates = new HashMap<>();

    public static DADrugReader drugReader;
    public static boolean logDrugLoadInfo;
    public static boolean logDrugLoadComplete;
    public static boolean logDrugLoadError;

    public static DASeedReader seedReader;
    public static boolean logSeedLoadInfo;
    public static boolean logSeedLoadComplete;
    public static boolean logSeedLoadError;

    public static int[] tableTitleArray = new int[]{0, 0, 0, 0};

    /**
     * Checks if the config exists and creates it if not
     * Also copies the default configs
     * If the config is not valid, the plugin will be disabled
     *
     * @return true if the config is valid
     */
    public static boolean checkConfig() {
        File file = new File(DA.getInstance.getDataFolder(), "config.yml");
        if (!file.exists()) {
            loader.errorLog(ChatColor.BOLD + "No config.yml found, creating default file! You may want to choose a config according to your language!");
            loader.log(ChatColor.BOLD + "You can find them in plugins/DrugsAdder/configs/");
            loader.log(ChatColor.BOLD + "Just copy the config for your language into the DrugsAdder folder and use /drugsadder reload");
            InputStream defConf = DA.getInstance.getResource("config/en/config.yml");
            if (defConf == null) {
                loader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
                return false;
            }
            try {
                DAUtil.saveFile(defConf, DA.getInstance.getDataFolder(), "config.yml", false);
            } catch (IOException e) {
                loader.errorLog(e.getMessage());
                for (StackTraceElement element : e.getStackTrace()) {
                    loader.log(element.toString());
                }
                return false;
            }
        }
        if (!file.exists()) {
            loader.errorLog("Default config file not found! Your jarfile might be corrupted. Disabling DrugsAdder");
            return false;
        }

        copyDefaultConfigs(false);
        return true;
    }

    /**
     * Copies the default configs into the plugins folder
     *
     * @param overwrite if true, the configs will be overwritten
     */
    private static void copyDefaultConfigs(boolean overwrite) {
        File configs = new File(DA.getInstance.getDataFolder(), "configs");
        File languages = new File(DA.getInstance.getDataFolder(), "languages");
        for (String l : new String[]{"de", "en"}) {
            File lFold = new File(configs, l);
            try {
                DAUtil.saveFile(DA.getInstance.getResource("config/" + l + "/config.yml"), lFold, "config.yml", overwrite);
                DAUtil.saveFile(DA.getInstance.getResource("languages/" + l + ".yml"), languages, l + ".yml", false); // Never overwrite languages, they get updated with their updater
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * Loads the config file
     *
     * @return the config file or null if the config is not valid
     */
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
                    Arrays.asList(e.getStackTrace()).forEach(stackTraceElement -> loader.log(stackTraceElement.toString()));
                } else {
                    loader.errorLog("Could not read file config.yml, please make sure the file is in valid yml format (correct spaces etc.)");
                }
            }
        }
        return fileConfiguration;
    }

    /**
     * Reads the config file and sets the variables
     * Also loads the language file
     * If the config is not valid, the plugin will be disabled
     *
     * @param config the config file
     */
    public static void readConfig(FileConfiguration config) throws NumberFormatException {

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

        // Check if Slimefun is installed
        hasSlimefun = Bukkit.getPluginManager().isPluginEnabled("Slimefun");
        //Check if ItemsAdder is installed
        hasItemsAdder = Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");


        //Loads the CancelRecipeItem
        cancelRecipeItem = config.getString("cancelRecipeItem", "drugsadder:cancel_recipe_item");
        //Loads the SuspiciousPotionItem
        suspiciousPotionItem = config.getString("suspiciousPotionItem", "drugsadder:suspicious_potion_item");

        //Loads the Data
        loadDataAsync = config.getBoolean("loadDataAsync", true);

        //Loads the return values
        returnBucket = config.getBoolean("returnBucket", true);
        returnBottle = config.getBoolean("returnBottle", true);
        //Loads the reset values
        resetItemCrafting = config.getBoolean("resetItemCrafting", true);
        resetItemSmelting = config.getBoolean("resetItemSmelting", true);

        //Title Array
        tableTitleArray = Arrays.stream(config.getString("tableTitleArray", "120,4,1,-10").split(",")).mapToInt(Integer::parseInt).toArray();
        if (tableTitleArray.length != 4) {
            tableTitleArray = new int[]{0, 0, 0, 0};
        }

        //Loads the Logging
        logCustomItemLoadInfo = config.getBoolean("logCustomItemLoadInfo", true);
        logCustomItemLoadComplete = config.getBoolean("logCustomItemLoadComplete", true);
        logCustomItemLoadError = config.getBoolean("logCustomItemLoadError", true);
        logRecipeLoadInfo = config.getBoolean("logRecipeLoadInfo", true);
        logRecipeLoadComplete = config.getBoolean("logRecipeLoadComplete", true);
        logRecipeLoadError = config.getBoolean("logRecipeLoadError", true);
        logDrugLoadInfo = config.getBoolean("logDrugLoadInfo", true);
        logDrugLoadComplete = config.getBoolean("logDrugLoadComplete", true);
        logDrugLoadError = config.getBoolean("logDrugLoadError", true);
        logSeedLoadInfo = config.getBoolean("logSeedLoadInfo", true);
        logSeedLoadComplete = config.getBoolean("logSeedLoadComplete", true);
        logSeedLoadError = config.getBoolean("logSeedLoadError", true);

        //Loads the TableStates
        if (config.contains("tableStates")) {
            for (String key : config.getConfigurationSection("tableStates").getKeys(false)) {
                tableStates.put(Integer.parseInt(key), config.getString("tableStates." + key));
            }
        }

        //Loads the own CustomItems
        if (config.contains("customItems")) {
            customItemReader = new DACustomItemReader(config.getConfigurationSection("customItems"));
            customItemReader.loadItems();
            if (!customItemReader.getRegisteredItems().containsKey("drugsadder:cancel_recipe_item")) {
                ItemStack itemStack = new ItemStack(Material.PAPER, 1);
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("§cCancel Recipe");
                itemMeta.setLore(List.of("§7This is the result of a cancelled recipe"));
                itemMeta.setCustomModelData(1);
                itemStack.setItemMeta(itemMeta);
                DAItem cancelRecipeItem = new DAItem(itemStack, "Cancel Recipe", List.of("§7This is the result of a cancelled recipe"), 1, "drugsadder:cancel_recipe_item");
                customItemReader.getRegisteredItems().put("drugsadder:cancel_recipe_item", cancelRecipeItem);
            }
        } else {
            customItemReader = new DACustomItemReader();
        }

        //Loads the Recipes
        if (config.contains("recipes")) {
            daRecipeReader = new DARecipeReader(config.getConfigurationSection("recipes"));
            daRecipeReader.loadRecipes();
        } else {
            daRecipeReader = new DARecipeReader();
        }

        //Loads the TableStates
        if (config.contains("tableStates")) {
            for (String key : config.getConfigurationSection("tableStates").getKeys(false)) {
                tableStates.put(Integer.parseInt(key), config.getString("tableStates." + key));
            }
        }


        //Loads the Drugs
        if (config.contains("drugs")) {
            drugReader = new DADrugReader(config.getConfigurationSection("drugs"));
            drugReader.loadDrugs();
        } else {
            drugReader = new DADrugReader();
            loader.errorLog(loader.languageReader.get("Load_Error_NoDrugs"));
        }

        //Loads the Seeds
        if (config.contains("seeds")) {
            seedReader = new DASeedReader(config.getConfigurationSection("seeds"));
            seedReader.loadSeeds();
        } else {
            seedReader = new DASeedReader();
            loader.errorLog(loader.languageReader.get("Load_Error_NoSeeds"));
        }

    }

    /**
     * Clears all the loaded data
     */
    public static void clear() {
        if (customItemReader != null) {
            customItemReader.getRegisteredItems().clear();
        }
        if (daRecipeReader != null) {
            daRecipeReader.getRegisteredRecipes().clear();
        }
        if (drugReader != null) {
            drugReader.getRegisteredDrugs().clear();
        }
        if (seedReader != null) {
            seedReader.getRegisteredSeeds().clear();
        }
    }

}
