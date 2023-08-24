package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.*;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DARecipeReader {


    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
    @Getter
    private final List<DARecipe> registeredRecipes = new ArrayList<>();
    private int configBarrelCount = 0;
    private int configPressCount = 0;
    private int configTableCount = 0;
    private int configCraftingCount = 0;
    private int configFurnaceCount = 0;

    public DARecipeReader(ConfigurationSection config) {
        this.config = config;
    }

    public DARecipeReader() {
        this.config = null;
    }

    public void loadRecipes() {
        assert config != null;
        Set<String> recipes = config.getKeys(false);
        for (String recipeType : recipes) {
            ConfigurationSection configSec = config.getConfigurationSection(recipeType);
            if (configSec == null) {
                this.logError("Load_Error_Recipe_NotConfigSection", recipeType);
            } else {
                switch (recipeType.toLowerCase()) {
                    case "barrel" -> this.loadBarrelRecipes(configSec);
                    case "press" -> this.loadPressRecipes(configSec);
                    case "table" -> this.loadTableRecipes(configSec);
                    case "crafting" -> this.loadCraftingRecipes(configSec);
                    case "furnace" -> this.loadFurnaceRecipes(configSec);
                    default -> this.logError("Load_Error_Recipe_UnknownType", recipeType);
                }
            }
        }

        this.completeLog();
    }

    private void loadBarrelRecipes(ConfigurationSection configSec) {
        Set<String> recipes = configSec.getKeys(false);
        this.configBarrelCount = recipes.size();
        for (String recipeID : recipes) {
            this.loadBarrelRecipe(recipeID, configSec);
        }
        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_Recipes", "Barrel");
        }
    }

    private void loadBarrelRecipe(String barrelRID, ConfigurationSection barrelCon) {
        ConfigurationSection recipeConfig = barrelCon.getConfigurationSection(barrelRID);
        if (recipeConfig == null) {
            this.logError("Load_Error_BarrelRecipe_NotConfigSection", barrelRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getRecipeNamedID().toLowerCase().equals(barrelRID))) {
            this.logError("Load_Error_BarrelRecipe_IDAlreadyAssigned", barrelRID);
            return;
        }
        DAItem result = this.getResultItem(barrelRID, recipeConfig);
        if (result == null) return;

        Map<String, DAItem> materials = new HashMap<>(this.loadMaterials(barrelRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", barrelRID);
            return;
        }

        int duration = recipeConfig.getInt("duration", 10);

        DABarrelRecipe barrelRecipe = new DABarrelRecipe(barrelRID, RecipeType.BARREL, duration, result, materials.values().toArray(new DAItem[0]));

        this.registeredRecipes.add(barrelRecipe);

        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_RecipeLoaded", barrelRID);
        }

    }

    private void loadPressRecipes(ConfigurationSection configSec) {
        Set<String> recipes = configSec.getKeys(false);
        this.configPressCount = recipes.size();
        for (String recipeID : recipes) {
            this.loadPressRecipe(recipeID, configSec);
        }
        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_Recipes", "Press");
        }
    }

    private void loadPressRecipe(String pressRID, ConfigurationSection pressCon) {
        ConfigurationSection recipeConfig = pressCon.getConfigurationSection(pressRID);
        if (recipeConfig == null) {
            this.logError("Load_Error_PressRecipe_NotConfigSection", pressRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getRecipeNamedID().toLowerCase().equals(pressRID))) {
            this.logError("Load_Error_PressRecipe_IDAlreadyAssigned", pressRID);
            return;
        }
        DAItem result = this.getResultItem(pressRID, recipeConfig);
        if (result == null) return;

        String moldString = recipeConfig.getString("mold", "null");
        DAItem mold = DAUtil.getItemStackByNamespacedID(moldString);
        if (mold == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", moldString, pressRID);
            return;
        }
        boolean returnMold = recipeConfig.getBoolean("returnMold", true);

        Map<String, DAItem> materials = new HashMap<>(this.loadMaterials(pressRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", pressRID);
            return;
        }

        DAPressRecipe pressRecipe = new DAPressRecipe(pressRID, RecipeType.PRESS, mold, returnMold, result, materials.values().toArray(new DAItem[0]));

        this.registeredRecipes.add(pressRecipe);

        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_RecipeLoaded", pressRID);
        }
    }

    private void loadTableRecipes(ConfigurationSection configSec) {
        Set<String> recipes = configSec.getKeys(false);
        this.configTableCount = recipes.size();
        for (String recipeID : recipes) {
            this.loadTableRecipe(recipeID, configSec);
        }
        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_Recipes", "Table");
        }
    }

    private void loadTableRecipe(String tableRID, ConfigurationSection tableSec) {
        ConfigurationSection recipeConfig = tableSec.getConfigurationSection(tableRID);
        if (recipeConfig == null) {
            this.logError("Load_Error_TableRecipe_NotConfigSection", tableRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getRecipeNamedID().toLowerCase().equals(tableRID))) {
            this.logError("Load_Error_TableRecipe_IDAlreadyAssigned", tableRID);
            return;
        }
        DAItem result = this.getResultItem(tableRID, recipeConfig);
        if (result == null) return;

        Map<String, DAItem> materials = new HashMap<>(this.loadMaterials(tableRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", tableRID);
            return;
        }

        DATableRecipe tableRecipe = new DATableRecipe(tableRID, RecipeType.TABLE, result, null, materials.values().toArray(new DAItem[0]));

        //this.registeredRecipes.add();

        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_RecipeLoaded", tableRID);
        }
    }

    private void loadCraftingRecipes(ConfigurationSection configSec) {
        Set<String> recipes = configSec.getKeys(false);
        this.configCraftingCount = recipes.size();
        for (String recipeID : recipes) {
            this.loadCraftingRecipe(recipeID, configSec);
        }
        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_Recipes", "Crafting");
        }
    }

    private void loadCraftingRecipe(String craftingRID, ConfigurationSection craftingSec) {
        ConfigurationSection recipeConfig = craftingSec.getConfigurationSection(craftingRID);
        if (recipeConfig == null) {
            this.logError("Load_Error_Recipe_NotConfigSection", craftingRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getRecipeNamedID().toLowerCase().equals(craftingRID))) {
            this.logError("Load_Error_Recipe_IDAlreadyAssigned", craftingRID);
            return;
        }
        DAItem result = this.getResultItem(craftingRID, recipeConfig);
        if (result == null) return;

        Map<String, DAItem> materials = new HashMap<>(this.loadMaterials(craftingRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", craftingRID);
            return;
        }
        if (materials.size() > 9) {
            this.logError("Load_Error_Recipes_TooManyMaterials", craftingRID);
            return;
        }

        List<String> shape = recipeConfig.getStringList("shape");
        if (shape.isEmpty()) {
            this.logError("Load_Error_Recipes_NoShape", craftingRID);
            return;
        }
        if (shape.size() != 3) {
            this.logError("Load_Error_Recipes_WrongShape", craftingRID);
            return;
        }
        for (String line : shape) {
            if (line.length() != 3) {
                this.logError("Load_Error_Recipes_WrongShape", craftingRID);
                return;
            }
        }
        for (String shapeKey : materials.keySet()) {
            if (shape.stream().noneMatch(line -> line.contains(shapeKey))) {
                this.logError("Load_Error_Recipes_ShapeKeyNotFound", shapeKey, craftingRID);
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder("[^");
        for (String key : materials.keySet()) {
            stringBuilder.append(key);
        }
        stringBuilder.append("]");
        String regex = stringBuilder.toString();

        DA.loader.debugLog(shape.toString());
        for (String s : new ArrayList<>(shape)) {
            shape.remove(s);
            shape.add(s.replaceAll(regex, " "));
        }
        DA.loader.debugLog(shape.toString());

        boolean isShaped = recipeConfig.getBoolean("isShaped", true);

        DACraftingRecipe craftingRecipe = new DACraftingRecipe(craftingRID, RecipeType.CRAFTING, result, materials.values().toArray(new DAItem[0]));
        craftingRecipe.setShapeless(!isShaped);
        craftingRecipe.setShape(shape.toArray(new String[0]));
        craftingRecipe.setShapeKeys(materials);

        try {
            if (!craftingRecipe.registerRecipe()) {
                this.logError("Load_Error_Recipes_RecipeNotRegistered", craftingRID);
                return;
            }
        } catch (Exception e) {
            StringBuilder log = new StringBuilder(e.getMessage());
            Arrays.stream(e.getStackTrace()).toList().forEach(stackTraceElement -> log.append("\n       ").append(stackTraceElement.toString()));
            DA.loader.errorLog(log.toString());
            return;
        }
        this.registeredRecipes.add(craftingRecipe);

        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_RecipeLoaded", craftingRID);
        }

    }

    private void loadFurnaceRecipes(ConfigurationSection configSec) {
        Set<String> recipes = configSec.getKeys(false);
        this.configFurnaceCount = recipes.size();
        for (String recipeID : recipes) {
            this.loadFurnaceRecipe(recipeID, configSec);
        }
        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_Recipes", "Furnace");
        }
    }

    private void loadFurnaceRecipe(String furnaceRID, ConfigurationSection furnaceSec) {
        ConfigurationSection recipeConfig = furnaceSec.getConfigurationSection(furnaceRID);
        if (recipeConfig == null) {
            this.logError("Load_Error_FurnaceRecipe_NotConfigSection", furnaceRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getRecipeNamedID().toLowerCase().equals(furnaceRID))) {
            this.logError("Load_Error_FurnaceRecipe_IDAlreadyAssigned", furnaceRID);
            return;
        }
        DAItem result = this.getResultItem(furnaceRID, recipeConfig);
        if (result == null) return;

        DAItem material = this.loadMaterial(furnaceRID, recipeConfig);
        if (material == null) {
            this.logError("Load_Error_Recipes_NoMaterial", furnaceRID);
            return;
        }
        int cookingTime = recipeConfig.getInt("cookingTime", -1);
        if (cookingTime == -1) {
            this.logError("Load_Error_Recipes_NoCookingTime", furnaceRID);
            return;
        }
        float exp = recipeConfig.getFloatList("exp").stream().findFirst().orElse(0f);
        DAFurnaceRecipe furnaceRecipe = new DAFurnaceRecipe(furnaceRID, RecipeType.FURNACE, result, material);
        furnaceRecipe.setCookingTime(cookingTime);
        furnaceRecipe.setExperience(exp);

        if (!furnaceRecipe.registerRecipe()) {
            this.logError("Load_Error_Recipes_RecipeNotRegistered", furnaceRID);
            return;
        }
        this.registeredRecipes.add(furnaceRecipe);

        if (DAConfig.logRecipeLoadInfo) {
            this.logInfo("Load_Info_RecipeLoaded", furnaceRID);
        }
    }

    @Nullable
    private DAItem getResultItem(String pressRID, ConfigurationSection recipeConfig) {
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
        if (result == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", resultAmount[0], pressRID);
            return null;
        }
        result.setAmount(amount);
        return result;
    }

    private DAItem loadMaterial(String recipeID, ConfigurationSection recipeConfig) {
        ConfigurationSection materialConfig = recipeConfig.getConfigurationSection("material");
        if (materialConfig == null) {
            this.logError("Load_Error_Recipes_NotConfigSection", recipeID);
            return null;
        }
        String namespacedID = materialConfig.getString("itemStack", "null");
        DAItem daItem = DAUtil.getItemStackByNamespacedID(namespacedID);
        if (daItem == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", namespacedID, recipeID);
            return null;
        }
        daItem.setAmount(materialConfig.getInt("amount", 1));
        List<ItemMatchType> itemMatchType = this.loadMatchTypes(recipeID, materialConfig);
        if (itemMatchType.isEmpty()) {
            return null;
        }
        daItem.setItemMatchTypes(itemMatchType.toArray(new ItemMatchType[0]));
        return daItem;
    }

    private Map<String, DAItem> loadMaterials(String recipeID, ConfigurationSection recipeConfig) {
        Map<String, DAItem> materials = new HashMap<>();
        ConfigurationSection materialsConfig = recipeConfig.getConfigurationSection("materials");
        if (materialsConfig == null) {
            this.logError("Load_Error_Recipes_NotConfigSection", recipeID);
            return materials;
        }

        for (String key : materialsConfig.getKeys(false)) {
            ConfigurationSection materialSec = materialsConfig.getConfigurationSection(key);
            if (materialSec != null) {
                String namespacedID = materialSec.getString("itemStack", "null");
                DAItem material = DAUtil.getItemStackByNamespacedID(namespacedID);
                if (material == null) {
                    this.logError("Load_Error_Recipes_ItemNotFound", namespacedID, recipeID);
                    continue;
                }
                material.setAmount(materialSec.getInt("amount", 1));
                List<ItemMatchType> itemMatchType = this.loadMatchTypes(recipeID, materialSec);
                if (itemMatchType.isEmpty()) {
                    continue;
                }
                material.setItemMatchTypes(itemMatchType.toArray(new ItemMatchType[0]));
                materials.put(key, material);
            } else {
                this.logError("Load_Error_Recipes_NotConfigSection", recipeID);
            }
        }
        return materials;
    }

    private List<ItemMatchType> loadMatchTypes(String recipeID, ConfigurationSection section) {
        List<ItemMatchType> matchTypes = new ArrayList<>();
        String matchType = section.getString("matchType", "NULL");
        if (matchType.contains(",")) {
            String[] types = matchType.split(",");
            for (String type : types) {
                ItemMatchType itemMatchType = ItemMatchType.valueOf(type);
                if (ItemMatchType.NULL.equals(itemMatchType)) {
                    this.logError("Load_Error_Recipes_MatchTypeNotFound", type, section.getName());
                    matchTypes.clear();
                    return matchTypes;
                } else {
                    matchTypes.add(itemMatchType);
                }
            }
        } else {
            ItemMatchType itemMatchType = ItemMatchType.valueOf(matchType);
            if (ItemMatchType.NULL.equals(itemMatchType)) {
                this.logError("Load_Error_Recipes_MatchTypeNotFound", matchType, recipeID);
                return matchTypes;
            }
            matchTypes.add(itemMatchType);
        }

        if (matchTypes.contains(ItemMatchType.ALL) && matchTypes.size() > 1) {
            this.logError("Load_Error_Recipes_MatchTypeWrongComposition", recipeID);
            matchTypes.clear();
            return matchTypes;
        }

        if (matchTypes.contains(ItemMatchType.CONTAINS_LORE) && matchTypes.contains(ItemMatchType.EXACT_LORE)) {
            this.logError("Load_Error_Recipes_MatchTypeWrongComposition", recipeID);
            matchTypes.clear();
            return matchTypes;
        }

        if (matchTypes.contains(ItemMatchType.CONTAINS_NAME) && matchTypes.contains(ItemMatchType.EXACT_NAME)) {
            this.logError("Load_Error_Recipes_MatchTypeWrongComposition", recipeID);
            matchTypes.clear();
            return matchTypes;
        }

        return matchTypes;
    }

    private void completeLog() {
        if (DAConfig.logRecipeLoadComplete) {
            long barrelCount = this.registeredRecipes.stream().filter(daRecipe -> daRecipe instanceof DABarrelRecipe).count();
            long pressCount = this.registeredRecipes.stream().filter(daRecipe -> daRecipe instanceof DAPressRecipe).count();
            long tableCount = this.registeredRecipes.stream().filter(daRecipe -> daRecipe instanceof DATableRecipe).count();
            long craftingCount = this.registeredRecipes.stream().filter(daRecipe -> daRecipe instanceof DACraftingRecipe).count();
            long furnaceCount = this.registeredRecipes.stream().filter(daRecipe -> daRecipe instanceof DAFurnaceRecipe).count();

            this.logInfo("Load_Info_RecipesComplete", ("" + barrelCount), ("" + this.configBarrelCount), ("" + pressCount), ("" + this.configPressCount), ("" + tableCount), ("" + this.configTableCount), ("" + craftingCount), ("" + this.configCraftingCount), ("" + furnaceCount), ("" + this.configFurnaceCount));
        }
    }

    private void logError(String key, String... args) {
        if (DAConfig.logRecipeLoadError) {
            LanguageReader languageReader = DA.loader.getLanguageReader();
            DALoader loader = DA.loader;
            if (languageReader != null) {
                loader.errorLog(languageReader.get(key, args));
            } else {
                loader.errorLog("Error while loading CustomItem " + args[0] + " - Skipping");
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

    public DARecipe getRecipe(String recipe) {
        return this.registeredRecipes.stream().filter(daRecipe -> daRecipe.getNamedID().equalsIgnoreCase(recipe)).findFirst().orElse(null);
    }
}
