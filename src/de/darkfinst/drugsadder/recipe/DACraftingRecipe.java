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
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class DACraftingRecipe extends DARecipe {

    private final List<String> shape = new ArrayList<>(3);
    private final Map<String, DAItem> shapeKeys = new HashMap<>();
    @Setter
    private boolean isShapeless = false;

    public DACraftingRecipe(String namedID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
    }

    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    public void setShapeKeys(@NotNull Map<String, DAItem> shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.putAll(shapeKeys);
    }

    public boolean registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getNamedID());
        if (this.isShapeless) {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
            for (DAItem material : this.getMaterials()) {
                shapelessRecipe.addIngredient(material.getItemStack().getType());
            }
            if (shapelessRecipe.getIngredientList().contains(null)) {
                return false;
            }
            return Bukkit.addRecipe(shapelessRecipe);
        } else {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
            shapedRecipe.shape(this.shape.toArray(new String[0]));
            for (String s : this.shape) {
                for (int i = 0; i < s.length(); i++) {
                    char key = s.charAt(i);
                    if (this.shapeKeys.containsKey(key + "")) {
                        Material material = this.shapeKeys.get(key + "").getItemStack().getType();
                        shapedRecipe.setIngredient(key, material);
                    } else {
                        return false;
                    }
                }
            }
            if (shapedRecipe.getIngredientMap().containsValue(null)) {
                return false;
            }
            return Bukkit.addRecipe(shapedRecipe);
        }
    }

    public static void registerDEMORecipe(boolean isShapeless) {
        ItemStack result = new ItemStack(Material.STICK, 1);
        ItemMeta meta = result.getItemMeta();
        if (isShapeless) {
            meta.setDisplayName("§6§lDEMO-Recipe-NoShape");
            result.setItemMeta(meta);
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(DA.getInstance, "demo_recipe_shapeless"), result);
            shapelessRecipe.addIngredient(Material.BIRCH_PLANKS);
            shapelessRecipe.addIngredient(Material.ACACIA_PLANKS);
            shapelessRecipe.addIngredient(Material.CHERRY_PLANKS);
            Bukkit.addRecipe(shapelessRecipe);
        } else {
            meta.setDisplayName("§6§lDEMO-Recipe-Shape");
            result.setItemMeta(meta);
            ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(DA.getInstance, "demo_recipe_shape"), result);
            shapedRecipe.shape(" A ", " B ", " C ");
            shapedRecipe.setIngredient('A', Material.SPRUCE_PLANKS);
            shapedRecipe.setIngredient('B', Material.DARK_OAK_PLANKS);
            shapedRecipe.setIngredient('C', Material.CHERRY_PLANKS);
            Bukkit.addRecipe(shapedRecipe);
        }
    }
}
