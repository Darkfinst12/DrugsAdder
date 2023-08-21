package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.RecipeChoice;

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
        RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(Arrays.stream(this.getMaterials()).findFirst().get().getItemStack());
        FurnaceRecipe furnaceRecipe = new FurnaceRecipe(namespacedKey, this.getResult().getItemStack(), exactChoice, this.getExperience(), this.getCookingTime());
        return Bukkit.addRecipe(furnaceRecipe);
    }
}
