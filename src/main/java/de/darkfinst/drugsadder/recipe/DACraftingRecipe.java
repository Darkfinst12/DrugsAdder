package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class DACraftingRecipe extends DARecipe {

    /**
     * The shape of the recipe
     */
    private final List<String> shape = new ArrayList<>(3);
    /**
     * The keys of the shape
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();
    /**
     * Whether the recipe is shapeless or not
     */
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

    /**
     * Registers the recipe in the server
     *
     * @return If the recipe was successfully registered
     */
    public boolean registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getID());
        if (this.isShapeless) {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
            for (DAItem material : this.getMaterials()) {
                RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(material.getItemStack());
                shapelessRecipe.addIngredient(exactChoice);
            }
            if (shapelessRecipe.getIngredientList().contains(null)) {
                return false;
            }
            if (!shapelessRecipe.equals(Bukkit.getRecipe(namespacedKey))) {
                Bukkit.removeRecipe(namespacedKey);
                return Bukkit.addRecipe(shapelessRecipe);
            } else {
                return true;
            }
        } else {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapedRecipe shapedRecipe = new ShapedRecipe(namespacedKey, result);
            shapedRecipe.shape(this.shape.toArray(new String[0]));
            for (String s : this.shape) {
                for (int i = 0; i < s.length(); i++) {
                    char key = s.charAt(i);
                    if (this.shapeKeys.containsKey(key + "")) {
                        RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(this.shapeKeys.get(key + "").getItemStack());
                        shapedRecipe.setIngredient(key, exactChoice);
                    } else {
                        return false;
                    }
                }
            }
            if (shapedRecipe.getIngredientMap().containsValue(null)) {
                return false;
            }
            if (!shapedRecipe.equals(Bukkit.getRecipe(namespacedKey))) {
                Bukkit.removeRecipe(namespacedKey);
                return Bukkit.addRecipe(shapedRecipe);
            } else {
                return true;
            }
        }
    }

    private void ifRegisteredUnregister(NamespacedKey namespacedKey) {
        Bukkit.removeRecipe(namespacedKey);

    }

    @Deprecated(since = "1.0.0", forRemoval = false)
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

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DACraftingRecipe")
                .replace("}", "") +
                ", shape=" + shape +
                ", shapeKeys=" + shapeKeys +
                ", isShapeless=" + isShapeless +
                "}";
    }
}
