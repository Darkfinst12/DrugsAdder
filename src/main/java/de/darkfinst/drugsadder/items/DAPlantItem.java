package de.darkfinst.drugsadder.items;

import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DAPlantItem extends DAItem implements Cloneable {

    /**
     * The growth time of the plant in seconds
     */
    private float growthTime;
    /**
     * Whether the plant should be destroyed on harvest
     */
    private boolean destroyOnHarvest;

    /**
     * The drops of the plant
     */
    private DAItem[] drops;
    /**
     * The allowed tools to harvest the plant
     */
    private Map<String, Integer> allowedTools = new HashMap<>();

    public DAPlantItem(@NotNull ItemStack itemStack, String namespacedID) {
        super(itemStack, namespacedID);
    }

    @Override
    public Component asListComponent() {
        Component component = super.asListComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.PLANT.getArg(), this.getNamespacedID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    @Override
    public Component getHover() {
        Component hover = super.getHover();
        hover = hover.appendNewline().append(Component.text("Growth Time: " + this.getGrowthTime() + "s"));
        hover = hover.appendNewline().append(Component.text("Destroy on Harvest: " + this.isDestroyOnHarvest()));
        hover = hover.appendNewline().append(Component.text("Drops: "));
        for (DAItem drop : this.getDrops()) {
            hover = hover.appendNewline().append(drop.getHover());
        }
        hover = hover.appendNewline().append(Component.text("Allowed Tools: "));
        for (Map.Entry<String, Integer> entry : this.getAllowedTools().entrySet()) {
            hover = hover.appendNewline().append(Component.text(entry.getKey() + ": " + entry.getValue()));
        }
        return hover;
    }

    @Override
    public DAPlantItem clone() {
        DAPlantItem clone = (DAPlantItem) super.clone();
        clone.setGrowthTime(this.getGrowthTime());
        clone.setDestroyOnHarvest(this.isDestroyOnHarvest());
        clone.setDrops(Arrays.copyOf(this.getDrops(), this.getDrops().length));
        return clone;
    }

    /**
     * @return Whether the item is a crop or not
     */
    public boolean isCrop() {
        return Material.BEETROOT_SEEDS.equals(this.getItemStack().getType()) || Material.CARROT.equals(this.getItemStack().getType())
                || Material.POTATO.equals(this.getItemStack().getType()) || Material.WHEAT_SEEDS.equals(this.getItemStack().getType())
                || Material.MELON_SEEDS.equals(this.getItemStack().getType()) || Material.PUMPKIN_SEEDS.equals(this.getItemStack().getType())
                ;
    }
}
