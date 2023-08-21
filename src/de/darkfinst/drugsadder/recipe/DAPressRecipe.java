package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class DAPressRecipe extends DARecipe {

    private final DAItem mold;
    private final boolean returnMold;

    public DAPressRecipe(String namedID,RecipeType recipeType, DAItem mold, boolean returnMold, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.returnMold = returnMold;
        this.mold = mold;
    }

    @Override
    public DAItem processMaterials(DAItem... givenMaterials) {
        List<DAItem> gml = Arrays.asList(givenMaterials);
        if (gml.contains(this.mold) && this.getMaterials().length == givenMaterials.length - 1) {
            for (DAItem material : this.getMaterials()) {
                if (!Arrays.asList(givenMaterials).contains(material)) {
                    return null;
                }
            }
            return this.getResult();
        }
        return null;
    }
}
