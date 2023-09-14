package de.darkfinst.drugsadder.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public DAPlantItem(@NotNull ItemStack itemStack, @NotNull String name, @NotNull List<String> lore, @NotNull Integer customModelData, String namespacedID) {
        super(itemStack, name, lore, customModelData, namespacedID);
    }

    public DAPlantItem(ItemStack itemStack) {
        super(itemStack);
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
        return Tag.ITEMS_VILLAGER_PLANTABLE_SEEDS.isTagged(this.getItemStack().getType());
    }
}
