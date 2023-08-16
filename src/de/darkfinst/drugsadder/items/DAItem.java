package de.darkfinst.drugsadder.items;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class DAItem {

    private final ItemStack itemStack;
    private final String namespacedID;

    public DAItem(ItemStack itemStack, String namespacedID) {
        this.itemStack = itemStack;
        this.namespacedID = namespacedID;
    }

    public DAItem(String namespacedID) {
        this.namespacedID = namespacedID;
        this.itemStack = DAItem.getItemStackByNamespacedID(namespacedID);
    }


    public static ItemStack getItemStackByNamespacedID(String namespacedID) {
        ItemStack itemStack = null;
        //TODO: CustomItem Check and get
        //TODO: Support: MMOItems - https://gitlab.com/phoenix-dvpmt/mmoitems
        //TODO: Support: Slimefun4 - https://github.com/Slimefun/Slimefun4
        //TODO: Support: ItemsAdder - https://github.com/LoneDev6/API-ItemsAdder
        //TODO: Support: OwnCustomItems

        return itemStack;
    }

    public static DAItem getDAItemByNamespacedID(String namespacedID) {
        return new DAItem(namespacedID);
    }
}
