package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class DAPressRecipe extends DARecipe {

    /**
     * The mold of the recipe
     */
    private final DAItem mold;
    /**
     * If the mold should be returned
     */
    private final boolean returnMold;
    /**
     * The duration of the process in seconds
     */
    private final double duration;

    public DAPressRecipe(String namedID, RecipeType recipeType, double duration, DAItem mold, boolean returnMold, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.duration = duration;
        this.returnMold = returnMold;
        this.mold = mold;
    }

    /**
     * Checks if the given items contain the mold
     *
     * @param givenItems The items to check
     * @return If the given items contain the mold
     */
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


    /**
     * Returns the material of the given item
     *
     * @param item The item to get the material from
     * @return The material of the given item or null if the item is not a material
     */
    @Override
    public @Nullable DAItem getMaterial(@NotNull ItemStack item) {
        if (DAUtil.matchItems(this.getMold().getItemStack(), item, this.getMold().getItemMatchTypes())) {
            return this.getMold();
        }
        return super.getMaterial(item);
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DAPressRecipe")
                .replace("}", "") +
                ", mold=" + mold +
                ", returnMold=" + returnMold +
                ", duration=" + duration +
                '}';
    }
}
