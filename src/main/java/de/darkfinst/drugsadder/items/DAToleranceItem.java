package de.darkfinst.drugsadder.items;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Currently not used
 */
@Getter
public class DAToleranceItem extends DAItem {

    private final int moreTolerance;
    private final int lessTolerance;


    public DAToleranceItem(ItemStack itemStack, String namespacedID, int moreTolerance, int lessTolerance) {
        super(itemStack, namespacedID);
        this.moreTolerance = moreTolerance;
        this.lessTolerance = lessTolerance;
    }
}
