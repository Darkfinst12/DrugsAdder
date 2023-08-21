package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

@Getter
public class DACraftingRecipe extends DARecipe {

    private final List<String> shape = new ArrayList<>(3);
    private final List<String> shapeKeys = new ArrayList<>();
    @Setter
    private boolean isShapeless = false;

    public DACraftingRecipe(String namedID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
    }

    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    public void setShapeKeys(String... shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.addAll(Arrays.asList(shapeKeys));
    }

    public boolean registerRecipe() {
        //TODO: You always get the Item after taking any item out of the crafting table
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getNamedID());
        if (this.isShapeless) {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
            for (DAItem material : this.getMaterials()) {
                shapelessRecipe.addIngredient(material.getItemStack().getType());
            }
            return Bukkit.addRecipe(shapelessRecipe);
        } else {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
            shapedRecipe.shape(this.shape.toArray(new String[0]));
            for (String s : this.shape) {
                char key = s.charAt(0);
                if (this.shapeKeys.contains(key + "")) {
                    int index = this.shapeKeys.indexOf(key + "");
                    Material material = this.getMaterials()[index].getItemStack().getType();
                    shapedRecipe.setIngredient(key, material);
                }
            }
            return Bukkit.addRecipe(shapedRecipe);
        }
    }
}
