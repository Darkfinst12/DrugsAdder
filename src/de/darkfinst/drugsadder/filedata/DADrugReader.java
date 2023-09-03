package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.*;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

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

        if(daAddiction.isAddictionAble()){
            drug.registerReductionTask();
        }

        registeredDrugs.add(drug);
        if (DAConfig.logDrugLoadInfo) {
            this.logInfo("Load_Info_Drug_Loaded", drugID);
        }
    }

    public DADrug getDrug(ItemStack item) {
        return this.registeredDrugs.stream().filter(drug -> DAUtil.matchItems(item, drug.getItemStack(), drug.getMatchTypes())).findFirst().orElse(null);
    }

    private DAEffect loadEffect(String effectString) {
        DAEffect effect = null;
        if (effectString.startsWith("PotionEffect")) {
            effect = this.loadPotionEffect(effectString);
        } else if (effectString.startsWith("ScreenEffect")) {
            effect = this.loadScreenEffect(effectString);
        }

        return effect;
    }

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

    private DAAddiction loadAddictionSettings(ConfigurationSection drugConfig) {
        DAAddiction daAddiction = new DAAddiction(false);
        ConfigurationSection addictionConfig = drugConfig.getConfigurationSection("addictionSettings");
        if (addictionConfig == null) {
            this.logError("Load_Error_Drug_Addiction", drugConfig.getName());
        } else {
            boolean isAddictionAble = addictionConfig.getBoolean("isAddictionAble", false);
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
                ConfigurationSection effectConfig = addictionConfig.getConfigurationSection("effects");
                Map<Integer, List<DAEffect>> effectMap = this.loadAddictionEffects(drugConfig, effectConfig);
                if (effectMap.isEmpty()) {
                    this.logError("Load_Error_Drug_Addiction_EffectsEmpty", drugConfig.getName());
                } else {
                    daAddiction.getConsummation().putAll(effectMap);
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

    private void logInfo(String key, String... args) {
        LanguageReader languageReader = DA.loader.getLanguageReader();
        DALoader loader = DA.loader;
        if (languageReader != null) {
            loader.log(languageReader.get(key, args));
        }
    }
}
