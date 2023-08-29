package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DAFurnaceRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.recipe.RecipeType;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

public class FurnaceStartSmeltEventListener implements Listener {

    public FurnaceStartSmeltEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onFurnaceStartSmeltEvent(FurnaceStartSmeltEvent event) {
        NamespacedKey key = event.getRecipe().getKey();
        if (key.getNamespace().equalsIgnoreCase(DA.getInstance.getName())) {
            DARecipe daRecipe = DAConfig.daRecipeReader.getRecipe(RecipeType.getNamedRecipeID(RecipeType.FURNACE, key.getKey()));
            if(daRecipe instanceof DAFurnaceRecipe furnaceRecipe){
                if(Arrays.stream(furnaceRecipe.getMaterials()).noneMatch(daItem -> DAUtil.matchItems(event.getSource(), daItem.getItemStack(), daItem.getItemMatchTypes()))){
                    DA.loader.debugLog("Cancel FurnaceStartSmeltEvent");
                    event.setTotalCookTime(furnaceRecipe.getCookingTime());
                    Furnace furnace = (Furnace) event.getBlock().getState();
                    event.setTotalCookTime(0);
                    furnace.setBurnTime((short) 0);
                    furnace.setCookTime((short) 0);
                    furnace.setCookTimeTotal((short) 0);
                    furnace.update();
                }

            }
        }

    }
}
