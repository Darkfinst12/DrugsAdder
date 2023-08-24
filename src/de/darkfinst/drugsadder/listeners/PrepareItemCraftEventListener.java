package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.filedata.LanguageReader;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DACraftingRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.Arrays;

public class PrepareItemCraftEventListener implements Listener {

    public PrepareItemCraftEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        Recipe recipe = event.getRecipe();
        if (recipe instanceof Keyed keyed) {
            NamespacedKey namespacedKey = keyed.getKey();
            if (namespacedKey.getNamespace().equalsIgnoreCase(DA.getInstance.getName())) {
                DA.loader.debugLog("Crafting Recipe: " + namespacedKey.getKey() + " - " + namespacedKey.getNamespace());
                this.checkCrafting(inv, namespacedKey.getKey());
            }
        }
    }

    private void checkCrafting(CraftingInventory inventory, String recipe) {
        //TODO: fix this
        DARecipe daRecipe = DAConfig.daRecipeReader.getRecipe(recipe);
        if (daRecipe instanceof DACraftingRecipe craftingRecipe) {
            ItemStack[] matrix = inventory.getMatrix();
            DAItem[] materials = craftingRecipe.getMaterials();
            if (craftingRecipe.isShapeless()) {
                for (DAItem material : materials) {
                    if (!Arrays.stream(matrix).allMatch(itemStack -> DAUtil.matchItems(material.getItemStack(), itemStack, material.getItemMatchTypes()))) {
                        inventory.setResult(null);
                        break;
                    }
                }
            } else {
                for (int i = 0; i < 9; i++) {
                    if (matrix[i] != null) {
                        DAItem material = materials[i];
                        if (!DAUtil.matchItems(material.getItemStack(), matrix[i], material.getItemMatchTypes())) {
                            inventory.setResult(null);
                            break;
                        }
                    }
                }
            }
        } else {
            inventory.setResult(null);
            this.logError("Error_Recipes_NotFound", recipe, inventory.getViewers().stream().findFirst().get().getName());
        }
    }

    private void logError(String key, String... args) {
        LanguageReader languageReader = DA.loader.getLanguageReader();
        DALoader loader = DA.loader;
        if (languageReader != null) {
            loader.errorLog(languageReader.get(key, args));
        } else {
            loader.errorLog("Error while crafting the Recipe: " + args[0] + "! - Player: " + args[1] + " attempted the recipe!");
        }
    }
}
