package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.*;

@Getter
public class DACraftingRecipe extends DARecipe {

    private final List<String> shape = new ArrayList<>(3);
    private final Set<String> shapeKeys = new HashSet<>();
    @Setter
    private boolean isShapeless = false;

    public DACraftingRecipe(String namedID, DAItem result, DAItem... materials) {
        super(namedID, result, materials);
    }

    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    public void setMaterials(String... shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.addAll(Arrays.asList(shapeKeys));
    }

    public void registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getNamedID());
        if (this.isShapeless) {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
            for (DAItem material : this.getMaterials()) {
                shapelessRecipe.addIngredient(material.getItemStack().getType());
            }
            Bukkit.addRecipe(shapelessRecipe);
        } else {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
            shapedRecipe.shape(this.shape.toArray(new String[0]));
            for (int i = 0; i < this.shape.size(); i++) {
                shapedRecipe.setIngredient(this.shapeKeys.toArray(new String[0])[i].charAt(0), this.getMaterials()[i].getItemStack().getType());
            }
            Bukkit.addRecipe(shapedRecipe);
        }
    }
}
