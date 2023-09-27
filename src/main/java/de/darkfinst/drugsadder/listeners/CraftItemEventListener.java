package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DACraftingRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.recipe.RecipeType;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

/**
 * This class handles the {@link org.bukkit.event.inventory.CraftItemEvent}
 */
public class CraftItemEventListener implements Listener {

    public CraftItemEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onCraftItemEvent(CraftItemEvent event) {
        CraftingInventory inv = event.getInventory();
        Recipe recipe = event.getRecipe();
        if (recipe instanceof Keyed keyed) {
            NamespacedKey namespacedKey = keyed.getKey();
            if (namespacedKey.getNamespace().equalsIgnoreCase(DA.getInstance.getName())) {
                this.checkDrops(inv, namespacedKey.getKey());
            }
        }
    }

    private void checkDrops(CraftingInventory inv, String recipe) {
        DARecipe daRecipe = DAConfig.daRecipeReader.getRecipe(RecipeType.getNamedRecipeID(RecipeType.CRAFTING, recipe));
        if (daRecipe instanceof DACraftingRecipe craftingRecipe) {
            if (DAConfig.returnBucket) {
                int returnBucketCount = Arrays.stream(craftingRecipe.getMaterials()).filter(daItem -> daItem.getItemStack().getType().equals(Material.WATER_BUCKET) || daItem.getItemStack().getType().equals(Material.LAVA_BUCKET)).toList().size();
                if (returnBucketCount > 0) {
                    ItemStack itemStack = new ItemStack(Material.BUCKET, returnBucketCount);
                    inv.setItem(4, itemStack);
                }
                if (DAConfig.returnBottle) {
                    int returnBottleCount = Arrays.stream(craftingRecipe.getMaterials()).filter(daItem -> daItem.getItemStack().getType().equals(Material.POTION)).toList().size();
                    if (returnBottleCount > 0) {
                        ItemStack itemStack = new ItemStack(Material.GLASS_BOTTLE, returnBottleCount);
                        inv.setItem(5, itemStack);
                    }
                }
            }
        }
    }
}
