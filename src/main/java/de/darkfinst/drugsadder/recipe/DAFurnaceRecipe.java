package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.Arrays;

@Getter
@Setter
public class DAFurnaceRecipe extends DARecipe {

    /**
     * The cooking time of the recipe
     */
    private int cookingTime;
    /**
     * The experience that is received by finishing the recipe
     */
    private float experience;

    public DAFurnaceRecipe(String namedID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
    }

    /**
     * Registers the recipe in the server
     *
     * @return If the recipe was successfully registered
     */
    public boolean registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getID());
        DAItem daItem = Arrays.stream(this.getMaterials()).findFirst().orElse(null);
        if (daItem == null) {
            return false;
        }
        RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(daItem.getItemStack());
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
        if (Bukkit.getRecipe(namespacedKey) == null) {
            return Bukkit.addRecipe(recipe);
        } else if (!recipe.equals(Bukkit.getRecipe(namespacedKey))) {
            Bukkit.removeRecipe(namespacedKey);
            return Bukkit.addRecipe(recipe);
        } else {
            return true;
        }
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DAFurnaceRecipe")
                .replace("}", "") +
                ", cookingTime=" + cookingTime +
                ", experience=" + experience +
                '}';
    }
}
