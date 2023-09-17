package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class DACrafterRecipe extends DARecipe {

    /**
     * The shape of the recipe
     */
    private final List<String> shape = new ArrayList<>(5);
    /**
     * The keys of the shape
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();
    /**
     * Whether the recipe is shapeless or not
     */
    @Setter
    private boolean isShapeless = false;

    /**
     * The processing time of the recipe
     */
    private final double processingTime;

    /**
     * The required players for the recipe
     */
    private final int requiredPlayers;

    public DACrafterRecipe(String ID, RecipeType recipeType, DAItem result, double processingTime, int requiredPlayers, DAItem... materials) {
        super(ID, recipeType, result, materials);
        this.processingTime = processingTime;
        this.requiredPlayers = requiredPlayers;
    }

    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    public void setShapeKeys(@NotNull Map<String, DAItem> shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.putAll(shapeKeys);
    }

    public boolean matchMaterials(ItemStack[] matrix) {
        if (this.getMaterials().length != matrix.length) {
            return false;
        }
        for (int i = 0; i < this.getMaterials().length; i++) {
            DAItem material = this.getMaterials()[i];
            if (!DAUtil.matchItems(material.getItemStack(), matrix[i], material.getItemMatchTypes())) {
                return false;
            }
        }
        return true;
    }

    public boolean matchShape(ItemStack[] matrix) throws IllegalArgumentException {
        if (this.isShapeless) {
            throw new IllegalArgumentException("Recipe is shapeless");
        }
        if (matrix.length != 25) {
            throw new IllegalArgumentException("Matrix length must be 25");
        }
        for (int i = 0; i < 5; i++) {
            String row = this.shape.get(i);
            String[] rowKeys = row.split(",");
            for (int j = 0; j < 5; j++) {
                String key = rowKeys[j];
                if (!key.equals(" ")) {
                    DAItem item = this.shapeKeys.get(key);
                    if (item == null || !DAUtil.matchItems(item.getItemStack(), matrix[i * 9 + j], item.getItemMatchTypes())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DACrafterRecipe")
                .replace("}", "") +
                ", shape=" + shape +
                ", shapeKeys=" + shapeKeys +
                ", isShapeless=" + isShapeless +
                "}";
    }
}
