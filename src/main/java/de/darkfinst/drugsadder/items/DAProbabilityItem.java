package de.darkfinst.drugsadder.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Setter
public class DAProbabilityItem extends DAItem {

    private double probability;

    public DAProbabilityItem(@NotNull ItemStack itemStack, String namespacedID) {
        super(itemStack, namespacedID);
    }

    public DAProbabilityItem(@NotNull ItemStack itemStack, @NotNull String name, @NotNull List<String> lore, @NotNull Integer customModelData, String namespacedID) {
        super(itemStack, name, lore, customModelData, namespacedID);
    }

    public DAProbabilityItem(ItemStack itemStack) {
        super(itemStack);
    }
}
