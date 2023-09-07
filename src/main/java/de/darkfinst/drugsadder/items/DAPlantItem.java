package de.darkfinst.drugsadder.items;

import de.darkfinst.drugsadder.ItemMatchType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class DAPlantItem extends DAItem implements Cloneable {

    private float growTime;
    private boolean destroyOnHarvest;
    private DAItem[] drops;

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
        clone.setGrowTime(this.getGrowTime());
        clone.setDestroyOnHarvest(this.isDestroyOnHarvest());
        clone.setDrops(Arrays.copyOf(this.getDrops(), this.getDrops().length));
        return clone;
    }

    public boolean isCrop() {
        return Tag.ITEMS_VILLAGER_PLANTABLE_SEEDS.isTagged(this.getItemStack().getType());
    }
}
