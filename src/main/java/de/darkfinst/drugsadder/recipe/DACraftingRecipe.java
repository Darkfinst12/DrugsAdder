package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
public class DACraftingRecipe extends DAShapedRecipe {

    /**
     * The shape of the recipe
     * <br>
     * The shape is a list of strings with a length of 3
     * <br>
     * Each string represents a row of the shape
     * <br>
     * Each character represents a slot in the row, that means the length of the string must be 3
     */
    private final List<String> shape = new ArrayList<>(3);

    /**
     * A list of keys for the shape, these keys are used to match the materials
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();

    /**
     * Whether the recipe is shapeless or not
     */
    @Setter
    private boolean isShapeless = false;

    public DACraftingRecipe(String recipeID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(recipeID, recipeType, result, materials);
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
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getRecipeID());
        if (this.isShapeless) {
            ItemStack result = this.getResult().getItemStack();
            result.setAmount(this.getResult().getAmount());
            ShapelessRecipe shapelessRecipe = new ShapelessRecipe(namespacedKey, result);
            for (DAItem material : this.getMaterials()) {
                ItemStack itemStack = material.getItemStack();
                itemStack.setAmount(material.getAmount());
                RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(itemStack);
                shapelessRecipe.addIngredient(exactChoice);
            }
            if (shapelessRecipe.getChoiceList().contains(null)) {
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
                        ItemStack itemStack = this.shapeKeys.get(key + "").getItemStack();
                        itemStack.setAmount(this.shapeKeys.get(key + "").getAmount());
                        RecipeChoice.ExactChoice exactChoice = new RecipeChoice.ExactChoice(itemStack);
                        shapedRecipe.setIngredient(key, exactChoice);
                    } else {
                        return false;
                    }
                }
            }
            if (shapedRecipe.getChoiceMap().containsValue(null)) {
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

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DACraftingRecipe")
                .replace("}", "") +
                ", shape=" + shape +
                ", shapeKeys=" + shapeKeys +
                ", isShapeless=" + isShapeless +
                "}";
    }

    /**
     * This method generates a component that represents the recipe.
     * <br>
     * It only shows the ID but extends a Hover Event that shows the process time and the materials.
     * <br>
     * It also extends a Click Event that executes the command to show the recipe in the info command.
     * <br>
     * For use see {@link de.darkfinst.drugsadder.commands.ListCommand}
     *
     * @return The component that represents the recipe.
     */
    @Override
    public @NotNull Component asListComponent() {
        Component component = super.asListComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommand(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.CRAFTING.getArg(), this.getRecipeID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * Returns the hover event of the recipe
     *
     * @return The hover event of the recipe
     */
    @Override
    //TODO: Make Translatable
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Shapeless: " + this.isShapeless + "\n"));
        if (!this.isShapeless) {
            hover = hover.append(Component.text("Shape:"));
            for (String row : this.shape) {
                hover = hover.appendNewline().append(Component.text(row));
            }
        }
        hover = super.getMaterialsAsComponent(hover, this.shapeKeys);
        return hover;
    }
}
