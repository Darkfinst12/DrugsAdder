package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public class DAPressRecipe extends DARecipe {

    private final DAItem mold;
    private final boolean returnMold;
    private final double duration;

    public DAPressRecipe(String namedID, RecipeType recipeType, double duration, DAItem mold, boolean returnMold, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.duration = duration;
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

    public boolean containsMold(ItemStack... givenItems) {
        boolean contains = false;
        for (ItemStack item : givenItems) {
            contains = DAUtil.matchItems(this.getMold().getItemStack(), item, this.getMold().getItemMatchTypes());
            if (contains) {
                break;
            }
        }
        return contains;
    }

    @Nullable
    @Override
    public DAItem getMaterial(@NotNull ItemStack item) {
        if (DAUtil.matchItems(this.getMold().getItemStack(), item, this.getMold().getItemMatchTypes())) {
            return this.getMold();
        }
        return super.getMaterial(item);
    }
}
