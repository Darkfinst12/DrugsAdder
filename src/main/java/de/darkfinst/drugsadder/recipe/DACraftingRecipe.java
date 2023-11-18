package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

@Getter
public class DACraftingRecipe extends DAShapedRecipe {

    public DACraftingRecipe(String recipeID, RecipeType recipeType, DAItem result, DAItem... materials) {
        super(recipeID, recipeType, result, materials);
    }

    /**
     * Registers the recipe in the server
     *
     * @return If the recipe was successfully registered
     */
    public boolean registerRecipe() {
        NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, this.getRecipeID());
        if (super.isShapeless()) {
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
            shapedRecipe.shape(super.getShape().toArray(new String[0]));
            for (String s : super.getShape()) {
                for (int i = 0; i < s.length(); i++) {
                    char key = s.charAt(i);
                    if (super.getShapeKeys().containsKey(key + "")) {
                        ItemStack itemStack = super.getShapeKeys().get(key + "").getItemStack();
                        itemStack.setAmount(super.getShapeKeys().get(key + "").getAmount());
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
                ", shape=" + super.getShape() +
                ", shapeKeys=" + super.getShapeKeys() +
                ", isShapeless=" + super.isShapeless() +
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
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.CRAFTING.getArg(), this.getRecipeID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * Returns the hover event of the recipe
     *
     * @return The hover event of the recipe
     */
    @Override
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = super.getShapeComponent(hover);
        hover = super.getMaterialsAsComponent(hover);
        return hover;
    }
}
