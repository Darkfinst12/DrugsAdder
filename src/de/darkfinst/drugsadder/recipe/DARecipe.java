package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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

    public boolean containsMaterials(@NotNull ItemStack... givenItems) {
        for (DAItem material : this.getMaterials()) {
            boolean contains = false;
            for (ItemStack item : givenItems) {
                contains = DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes());
                if (contains) {
                    break;
                }
            }
            if (!contains) {
                return false;
            }
        }
        return true;


       /* for (ItemStack item : givenItems) {
            for (DAItem material : this.getMaterials()) {
                boolean contains = DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes());
                if (contains) {
                    return true;
                }
            }
        }
        return false;*/
    }

    public String getRecipeNamedID() {
        return this.recipeType.name().toLowerCase() + ":" + this.namedID;
    }

    @Nullable
    public DAItem getMaterial(@NotNull ItemStack item) {
        return Arrays.stream(this.materials).filter(material -> DAUtil.matchItems(material.getItemStack(), item, material.getItemMatchTypes())).findFirst().orElse(null);
    }
}
