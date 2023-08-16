package de.darkfinst.drugsadder.items;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class DAItem {

    private final ItemStack itemStack;

    public DAItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }


}
