package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;

public class DACraftingRecipe extends DARecipe {


    protected DACraftingRecipe(String namedID, DAItem result, DAItem... materials) {
        super(namedID, result, materials);
    }
}
