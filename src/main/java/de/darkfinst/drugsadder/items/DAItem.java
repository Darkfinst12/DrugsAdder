package de.darkfinst.drugsadder.items;

import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class DAItem implements Cloneable {

    /**
     * The item, which represents the DAItem
     */
    @NotNull
    private final ItemStack itemStack;
    /**
     * The namespaced ID of the item
     */
    private final String namespacedID;
    /**
     * The name of the item
     */
    @Nullable
    private Component name;
    /**
     * The lore of the item
     */
    @Nullable
    private List<Component> lore;
    /**
     * The custom model data of the item
     */
    @Nullable
    private Integer customModelData;
    /**
     * The match types, which should be used to match the item
     */
    private ItemMatchType[] itemMatchTypes = new ItemMatchType[]{ItemMatchType.VANNILA};
    /**
     * The amount of the item
     */
    private int amount = 1;

    public DAItem(@NotNull ItemStack itemStack, String namespacedID) {
        this.itemStack = itemStack;
        if (itemStack.hasItemMeta()) {
            this.name = itemStack.getItemMeta().hasDisplayName() ? itemStack.getItemMeta().displayName() : Component.text(itemStack.getType().name());
            this.lore = itemStack.getItemMeta().lore();
            this.customModelData = itemStack.getItemMeta().getCustomModelData();
        }
        this.namespacedID = namespacedID;
    }

    public DAItem(@NotNull ItemStack itemStack, @NotNull Component name, @NotNull List<Component> lore, @NotNull Integer customModelData, String namespacedID) {
        this.itemStack = itemStack;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.namespacedID = namespacedID;
    }

    public DAItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.namespacedID = itemStack.getType().name();
        this.name = Component.text(itemStack.getType().name());
    }

    @Override
    public String toString() {
        return "DAItem{" +
                "itemStack=" + itemStack +
                ", name='" + name +
                ", lore=" + lore +
                ", customModelData=" + customModelData +
                ", itemMatchTypes=" + Arrays.toString(itemMatchTypes) +
                ", amount=" + amount +
                ", namespacedID='" + namespacedID + '\'' +
                '}';
    }

    @Override
    public DAItem clone() {
        try {
            DAItem clone = (DAItem) super.clone();
            clone.itemStack.setAmount(this.amount);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * This method generates a component that represents the DAItem.
     * <br>
     * It only shows the ID but extends a Hover Event that shows information about the item.
     * <br>
     * It also extends a Click Event that executes the command to show the item in the info command.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.ListCommand}.
     *
     * @return The component that represents the DAItem.
     */
    public Component asListComponent() {
        Component component = Component.text(this.namespacedID);
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommand(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.CUSTOM_ITEMS.getArg(), this.getNamespacedID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * This method generates a component that represents the DAItem.
     * <br>
     * It is used in the {@link de.darkfinst.drugsadder.commands.InfoCommand}.
     *
     * @return The component that represents the recipe.
     */
    public @NotNull Component asInfoComponent() {
        Component component = Component.text(this.namespacedID);
        component = component.appendNewline().append(this.getHover());
        return component;
    }

    /**
     * Returns the item as a component, which can be used in a message as a hover
     *
     * @return The hover as a component
     */
    public Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Base Item: " + this.itemStack.getType().name()));
        hover = hover.appendNewline().append(Component.text("Custom Model Data: " + this.customModelData));
        hover = hover.appendNewline().append(Component.text("Name: " + this.name));
        hover = hover.appendNewline().append(Component.text("Lore: " + this.lore));
        hover = hover.appendNewline().append(Component.text("Item Match Types: " + Arrays.toString(this.itemMatchTypes)));
        return hover;
    }
}
