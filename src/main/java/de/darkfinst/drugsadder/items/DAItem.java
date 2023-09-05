package de.darkfinst.drugsadder.items;

import de.darkfinst.drugsadder.ItemMatchType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class DAItem implements Cloneable{

    @NotNull
    private final ItemStack itemStack;
    @Nullable
    private String name;
    @Nullable
    private List<String> lore;
    @Nullable
    private Integer customModelData;

    private ItemMatchType[] itemMatchTypes = new ItemMatchType[]{ItemMatchType.VANNILA};
    private int amount = 1;

    private final String namespacedID;

    public DAItem(@NotNull ItemStack itemStack, String namespacedID) {
        this.itemStack = itemStack;
        if(itemStack.hasItemMeta()){
            this.name = itemStack.getItemMeta().getDisplayName();
            this.lore = itemStack.getItemMeta().getLore();
            this.customModelData = itemStack.getItemMeta().getCustomModelData();
        }
        this.namespacedID = namespacedID;
    }

    public DAItem(@NotNull ItemStack itemStack, @NotNull String name, @NotNull List<String> lore, @NotNull Integer customModelData, String namespacedID) {
        this.itemStack = itemStack;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.namespacedID = namespacedID;
    }

    public DAItem(ItemStack itemStack){
        this.itemStack = itemStack;
        this.namespacedID = itemStack.getType().name();
    }

    @Override
    public String toString() {
        return "DAItem{" +
                "itemStack=" + itemStack +
                ", name='" + name +
                ", lore=" + lore +
                ", customModelData=" + customModelData +
                ", itemMatchTypes=" + Arrays.toString(itemMatchTypes) +
                ", amount=" + amount +
                ", namespacedID='" + namespacedID + '\'' +
                '}';
    }

    @Override
    public DAItem clone() {
        try {
            DAItem clone = (DAItem) super.clone();
            clone.itemStack.setAmount(this.amount);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
