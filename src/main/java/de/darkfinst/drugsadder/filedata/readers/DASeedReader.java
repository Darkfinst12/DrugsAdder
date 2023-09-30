package de.darkfinst.drugsadder.filedata.readers;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.items.DAProbabilityItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class DASeedReader {

    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
    private final List<DAPlantItem> registeredSeeds = new ArrayList<>();
    private int configSeedsCount = 0;


    public DASeedReader(ConfigurationSection config) {
        this.config = config;
    }

    public DASeedReader() {
        this.config = null;
    }


    public void loadSeeds() {
        assert config != null;
        Set<String> seeds = config.getKeys(false);
        this.configSeedsCount = seeds.size();
        for (String seedID : seeds) {
            this.loadSeed(seedID);
        }
        if (DAConfig.logSeedLoadComplete) {
            this.logInfo("Load_Info_SeedsComplete", (this.registeredSeeds.size() + ""), (this.configSeedsCount + ""));
        }
    }

    private void loadSeed(String seedID) {
        assert config != null;
        ConfigurationSection seedConfig = config.getConfigurationSection(seedID);
        if (seedConfig == null) {
            this.logError("Load_Error_Seed_NotConfigSection", seedID);
            return;
        }
        if (this.registeredSeeds.stream().anyMatch(daDrug -> daDrug.getNamespacedID().equalsIgnoreCase(seedID))) {
            this.logError("Load_Error_Seed_IDAlreadyAssigned", seedID);
            return;
        }
        DAItem item = DAUtil.getItemStackByNamespacedID(seedID);
        if (item == null || !this.isValidSeed(item.getItemStack())) {
            this.logError("Load_Error_Seed_NotValid", seedID);
            return;
        }
        List<ItemMatchType> matchTypes = new ArrayList<>();
        String matchTypeCon = seedConfig.getString("matchType", "NULL");
        if (matchTypeCon.contains(",")) {
            for (String matchType : matchTypeCon.split(",")) {
                ItemMatchType itemMatchType = ItemMatchType.valueOf(matchType);
                if (ItemMatchType.NULL.equals(itemMatchType)) {
                    this.logError("Load_Error_Seed_MatchType", seedID, matchType);
                    return;
                } else {
                    matchTypes.add(itemMatchType);
                }
            }
        }
        item.setItemMatchTypes(matchTypes.toArray(new ItemMatchType[0]));

        List<DAItem> drops = new ArrayList<>();
        ConfigurationSection dropsConfig = seedConfig.getConfigurationSection("drops");
        if (dropsConfig != null) {
            Set<String> dropIDs = dropsConfig.getKeys(false);
            for (String dropID : dropIDs) {
                ConfigurationSection dropConfig = dropsConfig.getConfigurationSection(dropID);
                if (dropConfig == null) {
                    this.logError("Load_Error_Seed_Drop_NotConfigSection", seedID, dropID);
                    return;
                }
                String nameSpaceID = dropConfig.getString("itemStack", "NULL");
                DAItem drop = DAUtil.getItemStackByNamespacedID(nameSpaceID);
                if (drop == null) {
                    this.logError("Load_Error_Seed_Drop_ItemStack", seedID, dropID);
                    return;
                }
                DAProbabilityItem probabilityItem = new DAProbabilityItem(drop.getItemStack(), dropID);
                int amount = dropsConfig.getInt(dropID, 1);
                double probability = dropConfig.getDouble("probability", 100);
                if ((probability + "").length() > 7 || probability < 0 || probability > 100) {
                    this.logError("Load_Error_Seed_Drop_Probability", seedID, dropID, probability + "");
                    return;
                }
                probabilityItem.setProbability(probability);
                probabilityItem.setAmount(amount);
                drops.add(probabilityItem);
            }
        }

        boolean destroyOnHarvest = seedConfig.getBoolean("destroyOnHarvest", false);
        int growTime = seedConfig.getInt("growTime", 60);

        Map<String, Integer> allowedTools = this.getAllowedTools(seedConfig);
        if (allowedTools.isEmpty()) {
            this.logError("Load_Error_Seed_AllowedTools", seedID);
            return;
        }

        DAPlantItem plantItem = new DAPlantItem(item.getItemStack(), seedID);
        plantItem.setDestroyOnHarvest(destroyOnHarvest);
        plantItem.setGrowthTime(growTime);
        plantItem.setDrops(drops.toArray(new DAItem[0]));
        plantItem.setAllowedTools(allowedTools);


        registeredSeeds.add(plantItem);
        if (DAConfig.logSeedLoadInfo) {
            this.logInfo("Load_Info_Seed_Loaded", seedID);
        }
    }

    private boolean isValidSeed(ItemStack itemStack) {
        boolean isValid = false;
        if (itemStack != null) {
            Material plantType = itemStack.getType();
            isValid = Tag.CORAL_PLANTS.isTagged(plantType) || Tag.WALL_CORALS.isTagged(plantType)
                    || Tag.SMALL_FLOWERS.isTagged(plantType) || Tag.FLOWERS.isTagged(plantType)
                    || Tag.SAPLINGS.isTagged(plantType) || Tag.ITEMS_VILLAGER_PLANTABLE_SEEDS.isTagged(plantType)
                    || Material.NETHER_SPROUTS.equals(plantType) || Material.TALL_SEAGRASS.equals(plantType)
                    || Material.SEAGRASS.equals(plantType) || Material.FERN.equals(plantType)
                    || Material.TALL_GRASS.equals(plantType) || Material.LARGE_FERN.equals(plantType)
                    || Material.SWEET_BERRY_BUSH.equals(plantType) || Material.DEAD_BUSH.equals(plantType)
                    || Material.GLOW_BERRIES.equals(plantType) || Material.RED_MUSHROOM.equals(plantType)
                    || Material.BROWN_MUSHROOM.equals(plantType) || Material.WARPED_FUNGUS.equals(plantType)
                    || Material.CRIMSON_FUNGUS.equals(plantType)
            ;

        }
        return isValid;
    }

    public DAPlantItem getSeed(ItemStack item) {
        return this.registeredSeeds.stream().filter(seed -> DAUtil.matchItems(item, seed.getItemStack(), seed.getItemMatchTypes())).findFirst().orElse(null);
    }

    public DAPlantItem getSeed(String id) {
        return this.registeredSeeds.stream().filter(drug -> drug.getNamespacedID().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    public Map<String, Integer> getAllowedTools(ConfigurationSection seedConfig) {
        Map<String, Integer> allowedTools = new HashMap<>();
        if (seedConfig != null) {
            ConfigurationSection allowedToolsConfig = seedConfig.getConfigurationSection("allowedTools");
            if (allowedToolsConfig != null) {
                for (String allowedToolID : allowedToolsConfig.getKeys(false)) {
                    allowedTools.put(allowedToolID, allowedToolsConfig.getInt(allowedToolID));
                }
            }
        }
        return allowedTools;
    }

    /**
     * This method logs an error message to the console
     *
     * @param key  The key of the error message in the language file
     * @param args The arguments for the error message (optional)
     */
    private void logError(String key, String... args) {
        if (DAConfig.logSeedLoadError) {
            LanguageReader languageReader = DA.loader.getLanguageReader();
            DALoader loader = DA.loader;
            if (languageReader != null) {
                loader.errorLog(languageReader.get(key, args));
            } else {
                loader.errorLog("Error while loading seed " + args[0] + " - Skipping");
            }
        }
    }

    /**
     * This method logs an info message to the console
     *
     * @param key  The key of the info message in the language file
     * @param args The arguments for the info message (optional)
     */
    private void logInfo(String key, String... args) {
        LanguageReader languageReader = DA.loader.getLanguageReader();
        DALoader loader = DA.loader;
        if (languageReader != null) {
            loader.log(languageReader.get(key, args));
        }
    }

    public boolean isSeed(ItemStack itemStack) {
        return this.getSeed(itemStack) != null;
    }
}
