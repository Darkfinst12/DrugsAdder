package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.*;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class DARecipeReader {


    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
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
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getNamedID().toLowerCase().equals(barrelRID))) {
            this.logError("Load_Error_BarrelRecipe_IDAlreadyAssigned", barrelRID);
            return;
        }
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
        if (result == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", resultAmount[0], barrelRID);
            return;
        }
        result.setAmount(amount);

        List<DAItem> materials = new ArrayList<>(this.loadMaterials(barrelRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", barrelRID);
            return;
        }

        int duration = recipeConfig.getInt("duration", 10);

        DABarrelRecipe barrelRecipe = new DABarrelRecipe(barrelRID, duration, result, materials.toArray(new DAItem[0]));
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
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getNamedID().toLowerCase().equals(pressRID))) {
            this.logError("Load_Error_PressRecipe_IDAlreadyAssigned", pressRID);
            return;
        }
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
        if (result == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", resultAmount[0], pressRID);
            return;
        }
        result.setAmount(amount);

        String moldString = recipeConfig.getString("mold", "null");
        DAItem mold = DAUtil.getItemStackByNamespacedID(moldString);
        if (mold == null) {
            this.logError("Load_Error_Recipes_ItemNotFound", moldString, pressRID);
            return;
        }
        boolean returnMold = recipeConfig.getBoolean("returnMold", true);

        List<DAItem> materials = new ArrayList<>(this.loadMaterials(pressRID, recipeConfig));
        if (materials.isEmpty()) {
            this.logError("Load_Error_Recipes_NoMaterials", pressRID);
            return;
        }

        DAPressRecipe pressRecipe = new DAPressRecipe(pressRID, mold, returnMold, result, materials.toArray(new DAItem[0]));

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
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getNamedID().toLowerCase().equals(tableRID))) {
            this.logError("Load_Error_TableRecipe_IDAlreadyAssigned", tableRID);
            return;
        }
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
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
            this.logError("Load_Error_CraftingRecipe_NotConfigSection", craftingRID);
            return;
        }
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getNamedID().toLowerCase().equals(craftingRID))) {
            this.logError("Load_Error_CraftingRecipe_IDAlreadyAssigned", craftingRID);
            return;
        }
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
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
        if (this.registeredRecipes.stream().anyMatch(daRecipe -> daRecipe.getNamedID().toLowerCase().equals(furnaceRID))) {
            this.logError("Load_Error_FurnaceRecipe_IDAlreadyAssigned", furnaceRID);
            return;
        }
        String[] resultAmount = recipeConfig.getString("result", "null/1").split("/");
        int amount = Integer.parseInt(resultAmount[1]);
        DAItem result = DAUtil.getItemStackByNamespacedID(resultAmount[0]);
    }

    private List<DAItem> loadMaterials(String recipeID, ConfigurationSection recipeConfig) {
        List<DAItem> materials = new ArrayList<>();
        ConfigurationSection materialsConfig = recipeConfig.getConfigurationSection("materials");
        if (materialsConfig == null) {
            this.logError("Load_Error_Recipes_NotConfigSection", recipeID);
            return Collections.emptyList();
        }

        //TODO: Dose not work
        for (String key : materialsConfig.getKeys(false)) {
            ConfigurationSection materialSec = materialsConfig.getConfigurationSection(key);
            if (materialSec != null) {
                DAItem material = DAUtil.getItemStackByNamespacedID(materialSec.getString("itemStack", "null"));
                if (material == null) {
                    this.logError("Load_Error_Recipes_ItemNotFound", materialSec.getName(), recipeID);
                    continue;
                }
                material.setAmount(materialSec.getInt("amount", 1));
                ItemMatchType itemMatchType = ItemMatchType.valueOf(materialSec.getString("matchType", "NULL"));
                if (ItemMatchType.NULL.equals(itemMatchType)) {
                    this.logError("Load_Error_Recipes_MatchTypeNotFound", materialSec.getName(), recipeID);
                    continue;
                }
                material.setItemMatchType(itemMatchType);
                materials.add(material);
            } else {
                this.logError("Load_Error_Recipes_NotConfigSection", recipeID);
            }
        }
        return materials;
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
}
