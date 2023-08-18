package de.darkfinst.drugsadder.items;

import de.darkfinst.drugsadder.ItemMatchType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Getter
public class DAItem {

    @NotNull
    private final ItemStack itemStack;
    @Nullable
    private String name;
    @Nullable
    private List<String> lore;
    @Nullable
    private Integer customModelData;
    @Setter
    private ItemMatchType itemMatchType;
    @Setter
    private int amount = 1;

    private final String namespacedID;

    public DAItem(@NotNull ItemStack itemStack, String namespacedID) {
        this.itemStack = itemStack;
        this.namespacedID = namespacedID;
    }

    public DAItem(@NotNull ItemStack itemStack, @NotNull String name, @NotNull List<String> lore, @NotNull Integer customModelData, String namespacedID) {
        this.itemStack = itemStack;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
        this.namespacedID = namespacedID;
    }
}
