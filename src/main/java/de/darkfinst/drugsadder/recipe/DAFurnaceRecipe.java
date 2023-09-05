package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

@Getter
@Setter
public class DAFurnaceRecipe extends DARecipe {


    private int cookingTime;
    private float experience;

    public DAFurnaceRecipe(String namedID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
    }

    public boolean registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getNamedID());
        //TODO: FIX THIS - The itemMatchTypes are not used
        RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(Arrays.stream(this.getMaterials()).findFirst().get().getItemStack());
        Recipe recipe;
        switch (getRecipeType()) {
            case FURNACE ->
                    recipe = new FurnaceRecipe(namespacedKey, this.getResult().getItemStack(), exactChoice, this.getExperience(), this.getCookingTime());
            case SMOKING ->
                    recipe = new SmokingRecipe(namespacedKey, this.getResult().getItemStack(), exactChoice, this.getExperience(), this.getCookingTime());
            case BLASTING ->
                    recipe = new BlastingRecipe(namespacedKey, this.getResult().getItemStack(), exactChoice, this.getExperience(), this.getCookingTime());
            default -> {
                return false;
            }
        }
        return Bukkit.addRecipe(recipe);
    }

    public static void registerDEMORecipe() {
        ItemStack result = new ItemStack(Material.STICK, 1);
        ItemMeta meta = result.getItemMeta();
        meta.setDisplayName("§6§lDEMO-Recipe-F1");
        result.setItemMeta(meta);
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(new NamespacedKey(DA.getInstance, "demo_recipe_f1"), result, Material.ACACIA_PLANKS, 0.1f, 200);
        boolean successf1 = Bukkit.addRecipe(furnaceRecipe);
        DA.loader.debugLog("FurnaceRecipe F1 - " + successf1);
    }
}