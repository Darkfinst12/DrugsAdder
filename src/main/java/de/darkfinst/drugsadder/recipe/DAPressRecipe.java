package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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
     * The processing time of the recipe
     * <br>
     * The Time is in seconds
     */
    private final double processTime;

    public DAPressRecipe(String recipeID, RecipeType recipeType, double processTime, DAItem mold, boolean returnMold, DAItem result, DAItem... materials) {
        super(recipeID, recipeType, result, materials);
        this.processTime = processTime;
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
                ", duration=" + processTime +
                '}';
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
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.PRESS.getArg(), this.getRecipeID());
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
        hover = hover.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ProcessTime", this.getProcessTime() + ""));
        hover = hover.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_Mold"));
        Component name = this.getMold().getName();
        if (name == null) {
            name = Component.text(this.getMold().getItemStack().getType().name());
        }
        hover = hover.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_AmountX", this.mold.getAmount() + " ")).append(name);
        hover = hover.appendNewline().append(super.getMaterialsAsComponent());
        return hover;
    }
}
