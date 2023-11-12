package de.darkfinst.drugsadder.items;

import lombok.Getter;
import lombok.Setter;
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
