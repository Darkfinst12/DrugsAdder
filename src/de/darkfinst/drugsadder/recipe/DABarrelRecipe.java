package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAToleranceItem;
import lombok.Getter;

@Getter
public class DABarrelRecipe extends DARecipe {

    private final int processTime;

    public DABarrelRecipe(String namedID, int processTime, DAItem result, DAItem... materials) {
        super(namedID, result, materials);
        this.processTime = processTime;
    }

    @Override
    public DAItem processMaterials(DAItem... givenMaterials) {
        return super.processMaterials(givenMaterials);
    }
}
