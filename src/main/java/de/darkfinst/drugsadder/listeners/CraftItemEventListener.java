package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DACraftingRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.recipe.RecipeType;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                if (recipe instanceof ShapedRecipe shapedRecipe) {
                    RecipeChoice.ExactChoice[] choices = shapedRecipe.getChoiceMap().values().stream().map(recipeChoice -> (RecipeChoice.ExactChoice) recipeChoice).toArray(RecipeChoice.ExactChoice[]::new);
                    this.checkItems(inv, choices, shapedRecipe);
                } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                    RecipeChoice.ExactChoice[] choices = shapelessRecipe.getChoiceList().stream().map(recipeChoice -> (RecipeChoice.ExactChoice) recipeChoice).toArray(RecipeChoice.ExactChoice[]::new);
                    this.checkItems(inv, choices, shapelessRecipe);
                }
            }
        }
    }

    private void checkItems(CraftingInventory inv, RecipeChoice.ExactChoice[] choices, Recipe recipe) {
        Map<Integer, ItemStack> returns = new HashMap<>();
        for (int i = 0; i < choices.length; i++) {
            ItemStack matrixItem = inv.getMatrix()[i];
            if (matrixItem != null && choices[i].getItemStack().isSimilar(matrixItem)) {
                if (DAConfig.returnBucket && (Material.WATER_BUCKET.equals(matrixItem.getType())
                        || Material.LAVA_BUCKET.equals(matrixItem.getType()) || Material.MILK_BUCKET.equals(matrixItem.getType()))
                        || Material.AXOLOTL_BUCKET.equals(matrixItem.getType()) || Material.POWDER_SNOW_BUCKET.equals(matrixItem.getType())
                        || Material.SALMON_BUCKET.equals(matrixItem.getType()) || Material.TROPICAL_FISH_BUCKET.equals(matrixItem.getType())
                        || Material.PUFFERFISH_BUCKET.equals(matrixItem.getType()) || Material.COD_BUCKET.equals(matrixItem.getType())
                        || Material.TADPOLE_BUCKET.equals(matrixItem.getType())
                ) {
                    ItemStack bucket = new ItemStack(Material.BUCKET, matrixItem.getAmount());
                    returns.put(i, bucket);
                }

                if (DAConfig.returnBottle && Material.POTION.equals(matrixItem.getType())) {
                    ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE, matrixItem.getAmount());
                    returns.put(i, bottle);
                }
                int newAmount = matrixItem.getAmount() - (choices[i].getItemStack().getAmount() - 1);
                matrixItem.setAmount(newAmount);
            }
        }
        for (Map.Entry<Integer, ItemStack> entry : returns.entrySet()) {
            ItemStack itemStack = inv.getItem(entry.getKey());
            if (itemStack == null) {
                inv.setItem(entry.getKey(), entry.getValue());
            } else if (itemStack.getType().equals(entry.getValue().getType())) {
                itemStack.setAmount(itemStack.getAmount() + entry.getValue().getAmount());
            } else {
                Location location = inv.getLocation();
                if (location != null) {
                    location.getWorld().dropItem(location, entry.getValue());
                } else {
                    DA.log.errorLog(DA.loader.languageReader.get("Error_Crafting_ReturnItems", ((Keyed) recipe).getKey().asString(), entry.getValue().getType().name(), entry.getValue().getAmount() + ""));
                }
            }
        }

    }


    @Deprecated(since = "0.0.2", forRemoval = true)
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
