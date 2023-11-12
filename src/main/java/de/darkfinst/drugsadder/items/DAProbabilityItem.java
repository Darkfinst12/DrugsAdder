package de.darkfinst.drugsadder.items;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class DAProbabilityItem extends DAItem {

    /**
     * The probability to receive the item
     */
    private double probability;

    public DAProbabilityItem(@NotNull ItemStack itemStack, String namespacedID) {
        super(itemStack, namespacedID);
    }

}
