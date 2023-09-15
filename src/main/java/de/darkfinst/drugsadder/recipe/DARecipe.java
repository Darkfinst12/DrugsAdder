package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

@Getter
public abstract class DARecipe {

    /**
     * The id of the recipe
     */
    private final String ID;
    /**
     * The type of the recipe
     *
     * @see RecipeType
     */
    private final RecipeType recipeType;

    /**
     * The materials of the recipe
     */
    private DAItem[] materials;
    /**
     * The result of the recipe
     */
    private final DAItem result;

    protected DARecipe(String ID, RecipeType recipeType, DAItem result, DAItem... materials) {
        this.ID = ID;
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

    /**
     * Checks if the given items are materials of the recipe
     *
     * @param givenItems The items to check
     * @return If the given items are materials of the recipe
     */
    public boolean hasMaterials(ItemStack... givenItems) {
        for (DAItem material : this.getMaterials()) {
            boolean contains = false;
            for (ItemStack item : givenItems) {
                if (DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;
    }

    public void setMaterials(DAItem... materials) {
        this.materials = materials;
    }

    /**
     * Returns the named id of the recipe in the format of {@code <recipeType>:<namedID>}
     * <p>
     * Dose the same as {@link RecipeType#getNamedRecipeID(RecipeType, String)}
     *
     * @return The named id of the recipe
     */
    public String getRecipeNamedID() {
        return this.recipeType.name().toLowerCase() + ":" + this.ID;
    }

    /**
     * Returns the material of the given item
     *
     * @param item The item to get the material from
     * @return The material of the given item or null if the item is not a material
     */
    @Nullable
    public DAItem getMaterial(@NotNull ItemStack item) {
        return Arrays.stream(this.materials).filter(material -> DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes())).findFirst().orElse(null);
    }

    @Override
    public String toString() {
        return "DARecipe{" +
                "namedID='" + ID + '\'' +
                ", recipeType=" + recipeType +
                ", materials=" + Arrays.toString(materials) +
                ", result=" + result +
                '}';
    }
}
