package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;

@Getter
public class DABarrelRecipe extends DARecipe {

    private final int processTime;

    public DABarrelRecipe(String namedID, RecipeType recipeType, int processTime, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.processTime = processTime;
    }

    @Override
    public DAItem processMaterials(DAItem... givenMaterials) {
        return super.processMaterials(givenMaterials);
    }
}
