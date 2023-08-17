package de.darkfinst.drugsadder.items;

import lombok.Getter;
import net.Indyuce.mmoitems.api.MMOItemsAPI;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Getter
public class DAItem {

    private final ItemStack itemStack;
    @Nullable
    private String name;
    @Nullable
    private List<String> lore;
    @Nullable
    private Integer customModelData;

    private final String namespacedID;

    public DAItem(ItemStack itemStack, String namespacedID) {
        this.itemStack = itemStack;
        this.namespacedID = namespacedID;
    }

    public DAItem(String namespacedID) {
        this.namespacedID = namespacedID;
        this.itemStack = DAItem.getItemStackByNamespacedID(namespacedID);
    }

    public DAItem(ItemStack itemStack, @NotNull String name, @NotNull List<String> lore, @NotNull Integer customModelData, String namespacedID) {
        this.itemStack = itemStack;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.namespacedID = namespacedID;
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
