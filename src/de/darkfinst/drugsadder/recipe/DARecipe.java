package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;

import java.util.Arrays;

@Getter
public abstract class DARecipe {

    private final String namedID;
    private final RecipeType recipeType;

    private final DAItem[] materials;
    private final DAItem result;

    protected DARecipe(String namedID, RecipeType recipeType, DAItem result, DAItem... materials) {
        this.namedID = namedID;
        this.recipeType = recipeType;
        this.result = result;
        this.materials = materials;
    }

    public DAItem processMaterials(DAItem... givenMaterials) {
        if (this.materials.length == givenMaterials.length) {
            for (DAItem material : this.materials) {
                if (!Arrays.asList(givenMaterials).contains(material)) {
                    return null;
                }
            }
            return this.result;
        }
        return null;
    }

    public String getRecipeNamedID() {
        return this.recipeType.name().toLowerCase() + ":" + this.namedID;
    }

}
