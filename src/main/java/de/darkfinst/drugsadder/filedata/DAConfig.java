package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.filedata.readers.*;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DAConfig {

    /**
     * The current config version
     */
    private static final String configVersion = "0.1";

    /**
     * The loader of the plugin
     */
    private static final DALoader loader = DA.loader;

    /**
     * If the data should be loaded async
     */
    public static boolean loadDataAsync;

    /**
     * If the bucket should be returned after crafting
     */
    public static boolean returnBucket;
    /**
     * If the bottle should be returned after crafting
     */
    public static boolean returnBottle;
    /**
     * If the item should be reverted to the original item in a crafting table
     */
    public static boolean resetItemCrafting;
    /**
     * If the item should be reverted to the original item in a furnace, smoker or blast furnace
     */
    public static boolean resetItemSmelting;

    /**
     * If Slimefun is installed
     */
    public static boolean hasSlimefun;
    /**
     * If ItemsAdder is installed
     */
    public static boolean hasItemsAdder;


    /**
     * The item that is given back when a recipe is canceled
     */
    public static String cancelRecipeItem;
    /**
     * The item that is given when a table recipe is finished and combined with a wrong recipe
     */
    public static String suspiciousPotionItem;

    /**
     * The map of all table states - It must be 11 States
     * the key is the state of the table and the value is an unicode character
     */
    public static Map<Integer, String> tableStates = new HashMap<>();
    /**
     * The offset of the table title - It must be four numbers
     * It must be changed depending on the name of the table
     */
    public static int[] tableTitleArray = new int[]{0, 0, 0, 0};

    /**
     * The map of all crafter states - It must be eight States
     * the key is the state of the crafter and the value is an unicode character
     */
    public static Map<Integer, String> crafterStates = new HashMap<>();
    /**
     * The offset of the crafter title - It must be four numbers
     * It must be changed depending on the name of the table
     */
    public static int[] crafterTitleArray = new int[]{0, 0, 0, 0};
    /**
     * If the crafter should keep the inventory after closing
     */
    public static boolean crafterKeepInv;

    /**
     * The reader for the custom items
     */
    public static DACustomItemReader customItemReader;
    /**
     * If a load complete message should be logged of a custom item
     */
    public static boolean logCustomItemLoadInfo;
    /**
     * If a load complete message should be logged of all custom items
     */
    public static boolean logCustomItemLoadComplete;
    /**
     * If a load error message should be logged of the custom items
     */
    public static boolean logCustomItemLoadError;

    /**
     * The reader for the recipes
     */
    public static DARecipeReader daRecipeReader;
    /**
     * If a load complete message should be logged of a recipe
     */
    public static boolean logRecipeLoadInfo;
    /**
     * If a load complete message should be logged of all recipes
     */
    public static boolean logRecipeLoadComplete;
    /**
     * If a load error message should be logged of the recipes
     */
    public static boolean logRecipeLoadError;

    /**
     * The reader for the drugs
     */
    public static DADrugReader drugReader;
    /**
     * If a load complete message should be logged of a drug
     */
    public static boolean logDrugLoadInfo;
    /**
     * If a load complete message should be logged of all drugs
     */
    public static boolean logDrugLoadComplete;
    /**
     * If a load error message should be logged of the drugs
     */
    public static boolean logDrugLoadError;

    /**
     * The reader for the custom seeds
     */
    public static DASeedReader seedReader;
    /**
     * If a load complete message should be logged of a seed
     */
    public static boolean logSeedLoadInfo;
    /**
     * If a load complete message should be logged of all seeds
     */
    public static boolean logSeedLoadComplete;
    /**
     * If a load error message should be logged of the seeds
     */
    public static boolean logSeedLoadError;

    /**
     * If debug messages should be logged - Only recommended for developers
     * Default: false
     */
    public static boolean debugLogg;
    /**
     * If general info messages should be logged
     * Default: true
     */
    public static boolean logGeneralInfo;


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
            loader.errorLog(Component.text("No config.yml found, creating default file! You may want to choose a config according to your language!").decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
            loader.log(Component.text("You can find them in plugins/DrugsAdder/configs/").decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
            loader.log(Component.text("Just copy the config for your language into the DrugsAdder folder and use /drugsadder reload").decoration(TextDecoration.BOLD, true).decoration(TextDecoration.ITALIC, false));
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
        //Loads the keep values
        crafterKeepInv = config.getBoolean("crafterKeepInventory", false);

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
        debugLogg = config.getBoolean("debugLogg", false);
        logGeneralInfo = config.getBoolean("logGeneralInfo", true);

        //Loads the States
        if (config.contains("tableStates") && config.isConfigurationSection("tableStates")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("tableStates")).getKeys(false)) {
                tableStates.put(Integer.parseInt(key), config.getString("tableStates." + key));
            }
        }
        if (config.contains("crafterStates") && config.isConfigurationSection("crafterStates")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("crafterStates")).getKeys(false)) {
                crafterStates.put(Integer.parseInt(key), config.getString("crafterStates." + key));
            }
        }

        //Title Arrays
        tableTitleArray = Arrays.stream(config.getString("tableTitleArray", "120,4,1,-10").split(",")).mapToInt(DAUtil::parseInt).toArray();
        if (tableTitleArray.length != 4) {
            tableTitleArray = new int[]{0, 0, 0, 0};
        }
        crafterTitleArray = Arrays.stream(config.getString("crafterTitleArray", "120,4,1,-10").split(",")).mapToInt(DAUtil::parseInt).toArray();
        if (crafterTitleArray.length != 4) {
            crafterTitleArray = new int[]{0, 0, 0, 0};
        }

        //Loads the own CustomItems
        if (config.contains("customItems")) {
            customItemReader = new DACustomItemReader(config.getConfigurationSection("customItems"));
            customItemReader.loadItems();
            if (!customItemReader.getRegisteredItems().containsKey("drugsadder:cancel_recipe_item")) {
                DAItem cancelRecipeItem = getCancelRecipeItem();
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
     * Creates the cancel recipe item and returns it
     *
     * @return The cancel recipe item
     */
    private static @NotNull DAItem getCancelRecipeItem() {
        ItemStack itemStack = new ItemStack(Material.PAPER, 1);
        List<Component> lore = List.of(Component.text("This is the result of a cancelled recipe").color(NamedTextColor.GRAY));
        Component name = Component.text("Cancel Recipe").color(NamedTextColor.RED);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.displayName(name);
        itemMeta.lore(lore);
        itemMeta.setCustomModelData(1);
        itemStack.setItemMeta(itemMeta);
        return new DAItem(itemStack, name, lore, 1, "drugsadder:cancel_recipe_item");
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
        if (!crafterStates.isEmpty()) {
            crafterStates.clear();
        }
        if (!tableStates.isEmpty()) {
            tableStates.clear();
        }
    }

}
