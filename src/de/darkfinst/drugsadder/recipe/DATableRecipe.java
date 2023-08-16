package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;

@Getter
public class DATableRecipe extends DARecipe {

    private final DAItem filter;

    public DATableRecipe(String namedID, DAItem filter, DAItem result, DAItem... materials) {
        super(namedID, result, materials);
        this.filter = filter;
    }
}
