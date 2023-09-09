package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.*;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.stringtemplate.v4.ST;

import java.util.*;

@Getter
public class DADrugReader {

    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
    private final List<DADrug> registeredDrugs = new ArrayList<>();
    private int configDrugCount = 0;


    public DADrugReader(ConfigurationSection config) {
        this.config = config;
    }

    public DADrugReader() {
        this.config = null;
    }


    /**
     * This method loads all the Drugs from the config
     */
    public void loadDrugs() {
        assert config != null;
        Set<String> drugs = config.getKeys(false);
        this.configDrugCount = drugs.size();
        for (String drugID : drugs) {
            this.loadDrug(drugID);
        }
        if (DAConfig.logDrugLoadComplete) {
            this.logInfo("Load_Info_DrugsComplete", (this.registeredDrugs.size() + ""), (this.configDrugCount + ""));
        }
    }

    /**
     * This method loads a single drug from the config
     *
     * @param drugID The ID of the drug to load
     */
    private void loadDrug(String drugID) {
        assert config != null;
        ConfigurationSection drugConfig = config.getConfigurationSection(drugID);
        if (drugConfig == null) {
            this.logError("Load_Error_Drug_NotConfigSection", drugID);
            return;
        }
        if (this.registeredDrugs.stream().anyMatch(daDrug -> daDrug.getID().equals(drugID))) {
            this.logError("Load_Error_Drug_IDAlreadyAssigned", drugID);
            return;
        }
        String namespacedID = drugConfig.getString("itemStack", "null");
        DAItem item = DAUtil.getItemStackByNamespacedID(namespacedID);
        if (item == null) {
            this.logError("Load_Error_Drug_ItemStack", drugID, namespacedID);
            return;
        }
        List<ItemMatchType> matchTypes = new ArrayList<>();
        String matchTypeCon = drugConfig.getString("matchType", "NULL");
        if (matchTypeCon.contains(",")) {
            for (String matchType : matchTypeCon.split(",")) {
                ItemMatchType itemMatchType = ItemMatchType.valueOf(matchType);
                if (ItemMatchType.NULL.equals(itemMatchType)) {
                    this.logError("Load_Error_Drug_MatchType", drugID, matchType);
                    return;
                } else {
                    matchTypes.add(itemMatchType);
                }
            }
        }
        String consumeMessage = drugConfig.getString("consumeMessage", null);
        String consumeTitle = drugConfig.getString("consumeTitle", null);

        DADrug drug = new DADrug(drugID, item.getItemStack(), consumeMessage, consumeTitle, matchTypes.toArray(new ItemMatchType[0]));
        List<String> serverCommands = drugConfig.getStringList("serverCommands");
        drug.getServerCommands().addAll(serverCommands);
        List<String> playerCommands = drugConfig.getStringList("playerCommands");
        drug.getPlayerCommands().addAll(playerCommands);

        List<String> effects = drugConfig.getStringList("effects");
        List<DAEffect> daEffects = new ArrayList<>();
        for (String effect : effects) {
            DAEffect daEffect = this.loadEffect(effect);
            if (daEffect == null) {
                this.logError("Load_Error_Drug_Effect", drugID, effect);
                return;
            } else {
                daEffects.add(daEffect);
            }
        }
        drug.getDaEffects().addAll(daEffects);

        DAAddiction daAddiction = this.loadAddictionSettings(drugConfig);
        drug.setAddiction(daAddiction);

        if (daAddiction.isAddictionAble()) {
            drug.registerReductionTask();
        }

        registeredDrugs.add(drug);
        if (DAConfig.logDrugLoadInfo) {
            this.logInfo("Load_Info_Drug_Loaded", drugID);
        }
    }

    /**
     * This method returns a drug by its itemStack
     *
     * @param item The itemStack to get the drug from
     * @return The drug or null if no drug was found
     */
    public DADrug getDrug(ItemStack item) {
        return this.registeredDrugs.stream().filter(drug -> DAUtil.matchItems(item, drug.getItemStack(), drug.getMatchTypes())).findFirst().orElse(null);
    }

    /**
     * This method returns a drug by its ID
     *
     * @param id The ID of the drug to get
     * @return The drug or null if no drug was found
     */
    public DADrug getDrug(String id) {
        return this.registeredDrugs.stream().filter(drug -> drug.getID().equalsIgnoreCase(id)).findFirst().orElse(null);
    }

    /**
     * This method loads a single effect from a string
     *
     * @param effectString The string to load the effect from
     * @return The loaded effect or null if the effect could not be loaded
     */
    private DAEffect loadEffect(String effectString) {
        DAEffect effect = null;
        if (effectString.startsWith("PotionEffect")) {
            effect = this.loadPotionEffect(effectString);
        } else if (effectString.startsWith("ScreenEffect")) {
            effect = this.loadScreenEffect(effectString);
        }

        return effect;
    }

    /**
     * This method loads a potion effect from a string
     *
     * @param effectString The string to load the effect from
     * @return The loaded effect or null if the effect could not be loaded
     */
    private DAEffect loadPotionEffect(String effectString) {
        effectString = effectString.replace("PotionEffect{", "").replace("}", "");
        Map<String, String> map = DAUtil.parsMap(effectString);

        PotionEffectType effectType = PotionEffectType.getByName(Objects.requireNonNullElse(map.get("type"), "null"));
        int minDuration = Integer.parseInt(Objects.requireNonNullElse(map.get("minDuration"), "-1"));
        int maxDuration = Integer.parseInt(Objects.requireNonNullElse(map.get("maxDuration"), "-1"));
        int minLevel = Integer.parseInt(Objects.requireNonNullElse(map.get("minLevel"), "-1"));
        int maxLevel = Integer.parseInt(Objects.requireNonNullElse(map.get("maxLevel"), "-1"));
        float probability = Float.parseFloat(Objects.requireNonNullElse(map.get("probability"), "-1"));
        if (effectType == null || minDuration == -1 || maxDuration == -1 || maxLevel == -1 || minLevel == -1 || probability == -1) {
            return null;
        }
        Boolean particles = Boolean.parseBoolean(Objects.requireNonNullElse(map.get("particles"), "true"));
        Boolean icon = Boolean.parseBoolean(Objects.requireNonNullElse(map.get("icon"), "true"));

        return new DAEffect(minDuration, maxDuration, probability, effectType.getName(), minLevel, maxLevel, particles, icon);
    }

    /**
     * This method loads a screen effect from a string
     *
     * @param effectString The string to load the effect from
     * @return The loaded effect or null if the effect could not be loaded
     */
    private DAEffect loadScreenEffect(String effectString) {
        effectString = effectString.replace("ScreenEffect{", "").replace("}", "");
        Map<String, String> map = DAUtil.parsMap(effectString);

        int minDuration = Integer.parseInt(Objects.requireNonNullElse(map.get("minDuration"), "-1"));
        int maxDuration = Integer.parseInt(Objects.requireNonNullElse(map.get("maxDuration"), "-1"));
        float probability = Float.parseFloat(Objects.requireNonNullElse(map.get("probability"), "-1"));
        String screenEffect = map.get("screenEffect");

        if (minDuration == -1 || maxDuration == -1 || probability == -1 || screenEffect == null) {
            return null;
        }

        return new DAEffect(minDuration, maxDuration, probability, screenEffect);
    }

    /**
     * This method loads the addiction settings from the config
     *
     * @param drugConfig The config to load the settings from
     * @return The loaded settings
     */
    private DAAddiction loadAddictionSettings(ConfigurationSection drugConfig) {
        DAAddiction daAddiction = new DAAddiction(false);
        ConfigurationSection addictionConfig = drugConfig.getConfigurationSection("addictionSettings");
        if (addictionConfig == null) {
            this.logError("Load_Error_Drug_Addiction", drugConfig.getName());
        } else {
            boolean isAddictionAble = addictionConfig.getBoolean("addictionAble", false);
            boolean reductionOnlyOnline = addictionConfig.getBoolean("reductionOnlyOnline", false);
            int addictionPoints = addictionConfig.getInt("addictionPoints", -1);
            int overdose = addictionConfig.getInt("overdose", -1);
            int reductionAmount = addictionConfig.getInt("reductionAmount", -1);
            int reductionTime = addictionConfig.getInt("reductionTime", -1);
            daAddiction.setAddictionAble(isAddictionAble);
            daAddiction.setReductionOnlyOnline(reductionOnlyOnline);
            daAddiction.setAddictionPoints(addictionPoints);
            daAddiction.setOverdose(overdose);
            daAddiction.setReductionAmount(reductionAmount);
            daAddiction.setReductionTime(reductionTime);

            if (daAddiction.isAddictionAble()) {
                ConfigurationSection effectConfig = addictionConfig.getConfigurationSection("consummation");
                Map<Integer, List<DAEffect>> consummationMap = this.loadAddictionEffects(drugConfig, effectConfig);
                if (consummationMap.isEmpty()) {
                    this.logError("Load_Error_Drug_Addiction_EffectsEmpty", drugConfig.getName());
                } else {
                    daAddiction.getConsummation().putAll(consummationMap);
                }

                ConfigurationSection deprivationConfig = addictionConfig.getConfigurationSection("deprivation");
                Map<Integer, List<DAEffect>> deprivationMap = this.loadAddictionDeprivations(drugConfig, deprivationConfig);
                if (deprivationMap.isEmpty()) {
                    this.logError("Load_Error_Drug_Addiction_DeprivationEmpty", drugConfig.getName());
                } else {
                    daAddiction.getDeprivation().putAll(deprivationMap);
                }
            }
        }
        return daAddiction;
    }


    /**
     * This method loads all the consummation effects from the config
     *
     * @param drugConfig   The config to load the effects from
     * @param effectConfig The config section to load the effects from
     * @return The loaded effects as a map with the addiction level as key
     */
    private Map<Integer, List<DAEffect>> loadAddictionEffects(ConfigurationSection drugConfig, ConfigurationSection effectConfig) {
        Map<Integer, List<DAEffect>> effectMap = new HashMap<>();
        if (effectConfig == null) {
            this.logError("Load_Error_Drug_Addiction_Effects", drugConfig.getName());
        } else {
            for (String effectID : effectConfig.getKeys(false)) {
                List<String> effects = effectConfig.getStringList(effectID);
                if (effects.isEmpty()) {
                    this.logError("Load_Error_Drug_Addiction_Effects_EffectEmpty", drugConfig.getName(), effectID);
                } else {
                    this.loadEffectMap(drugConfig, effectMap, effectID, effects);
                }
            }
        }
        return effectMap;
    }

    /**
     * This method loads all the deprivation effects from the config
     *
     * @param drugConfig        The config to load the effects from
     * @param deprivationConfig The config section to load the effects from
     * @return The loaded effects as a map with the deprivation level as key
     */
    private Map<Integer, List<DAEffect>> loadAddictionDeprivations(ConfigurationSection drugConfig, ConfigurationSection deprivationConfig) {
        Map<Integer, List<DAEffect>> deprivationMap = new HashMap<>();
        if (deprivationConfig == null) {
            this.logError("Load_Error_Drug_Addiction_Deprivation", drugConfig.getName());
        } else {
            for (String deprivationID : deprivationConfig.getKeys(false)) {
                List<String> effects = deprivationConfig.getStringList(deprivationID);
                if (effects.isEmpty()) {
                    this.logError("Load_Error_Drug_Effect", drugConfig.getName(), deprivationID);
                } else {
                    this.loadEffectMap(drugConfig, deprivationMap, deprivationID, effects);
                }
            }
        }
        return deprivationMap;
    }

    /**
     * This method loads a map of effects from a list of effects
     *
     * @param drugConfig The config to load the effects from
     * @param map        The map to load the effects into
     * @param id         The ID of the effect
     * @param effects    The list of effects to load
     */
    private void loadEffectMap(ConfigurationSection drugConfig, Map<Integer, List<DAEffect>> map, String id, List<String> effects) {
        List<DAEffect> daEffects = new ArrayList<>();
        for (String effect : effects) {
            DAEffect daEffect = this.loadEffect(effect);
            if (daEffect == null) {
                this.logError("Load_Error_Drug_Effect", drugConfig.getName(), effect);
            } else {
                daEffects.add(daEffect);
            }
        }
        map.put(Integer.parseInt(id), daEffects);
    }

    /**
     * This method logs an error message to the console
     *
     * @param key  The key of the error message in the language file
     * @param args The arguments for the error message (optional)
     */
    private void logError(String key, String... args) {
        if (DAConfig.logDrugLoadError) {
            LanguageReader languageReader = DA.loader.getLanguageReader();
            DALoader loader = DA.loader;
            if (languageReader != null) {
                loader.errorLog(languageReader.get(key, args));
            } else {
                loader.errorLog("Error while loading drug " + args[0] + " - Skipping");
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

    public List<String> getDrugNames() {
        List<String> names = new ArrayList<>();
        for (DADrug registeredDrug : this.registeredDrugs) {
            names.add(registeredDrug.getID());
        }
        return names;
    }
}
