package de.darkfinst.drugsadder.structures;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class DAInvStructure extends DAStructure implements InventoryHolder {

    /**
     * The inventory of the structure
     */
    @Setter(AccessLevel.NONE)
    protected Inventory inventory;

    private final String translationKey;

    public DAInvStructure(String translationKey, int size) {
        this.translationKey = translationKey;
        this.inventory = DA.getInstance.getServer().createInventory(this, size, this.getTitle(0));
    }

    /**
     * Gets the title of the structure with the given state
     *
     * @param state The state of the table
     * @return The title of the table
     */
    public Component getTitle(int state) {
        String title = DA.loader.languageReader.get(this.translationKey);
        Component titleComp = MiniMessage.miniMessage().deserialize(title);
        title = LegacyComponentSerializer.legacyAmpersand().serialize(titleComp);
        int[] titleArray = DAConfig.tableTitleArray;
        String stateString = (DAUtil.convertWidthToMinecraftCode((title.length() * titleArray[0]) - titleArray[1]) + DAConfig.tableStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(title.length() * titleArray[2]) + titleArray[3]));

        return Component.text(stateString).color(NamedTextColor.WHITE).append(titleComp);
    }

    /**
     * Gets the title of the structure with the given state
     * <p>
     * Note this is a debug method
     *
     * @param m1    The first multiplier
     * @param m2    The first subtractor
     * @param m3    The second multiplier
     * @param m4    The second adder
     * @param state The state of the table
     * @return The title of the table
     */
    public Component getTitle(int m1, int m2, int m3, int m4, int state) {
        String title = DA.loader.languageReader.get(this.translationKey);
        Component titleComp = MiniMessage.miniMessage().deserialize(title);
        title = LegacyComponentSerializer.legacyAmpersand().serialize(titleComp);
        String stateString = DAUtil.convertWidthToMinecraftCode((title.length() * m1) - m2) + DAConfig.tableStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(title.length() * m3) + m4);
        return Component.text(stateString).color(NamedTextColor.WHITE).append(titleComp);
    }

    /**
     * Handles the inventory click event
     * <p>
     * Depending on the action, it cancels the event or calls the recipe check
     *
     * @param event The event to handle
     */
    public abstract void handleInventoryClick(InventoryClickEvent event);

    /**
     * Handles the inventory drag event
     *
     * @param event The event to handle
     */
    public abstract void handleInventoryDrag(InventoryDragEvent event);


    /**
     * Drops the inventory of the table
     */
    @Override
    public void destroyInventory() {
        ConcurrentLinkedDeque<HumanEntity> viewers = new ConcurrentLinkedDeque<>(this.inventory.getViewers());
        for (HumanEntity viewer : viewers) {
            if (viewer != null) {
                viewer.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
            }
        }
        this.dropContents();
    }

    public void dropContents() {
        for (ItemStack content : this.inventory.getContents()) {
            if (content != null && !content.getType().equals(Material.AIR)) {
                this.getBody().getWorld().dropItemNaturally(this.getBody().getSign().getLocation(), content);
            }
        }
        this.inventory.clear();
    }


    /**
     * @return The inventory of the table
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * It is an override of {@link DAStructure#hasInventory()}
     *
     * @return true
     */
    @Override
    public boolean hasInventory() {
        return true;
    }


}
