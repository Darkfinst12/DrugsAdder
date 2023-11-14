package de.darkfinst.drugsadder.recipe;

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
     * The duration of the process in seconds
     */
    private final double duration;

    public DAPressRecipe(String namedID, RecipeType recipeType, double duration, DAItem mold, boolean returnMold, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.duration = duration;
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
                ", duration=" + duration +
                '}';
    }

    @Override
    public Component asComponent() {
        Component component = super.asComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommand(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.PRESS.getArg(), this.getID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    @Override
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Duration: " + this.getDuration() + "s\n"));
        hover = hover.append(Component.text("Mold: "));
        Component name = this.getMold().getName();
        if (name == null) {
            name = Component.text(this.getMold().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getMold().getAmount() + " ")).append(name).append(Component.text("\n"));
        hover = hover.append(super.getMaterialsAsComponent());
        return hover;
    }
}
