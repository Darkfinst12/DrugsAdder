package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * This class handles the {@link org.bukkit.event.inventory.InventoryClickEvent}
 */
public class InventoryClickEventListener implements Listener {

    public InventoryClickEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        DAStructure daStructure = DA.loader.getStructure(event.getInventory());
        if (daStructure instanceof DABarrel daBarrel) {
            daBarrel.handleInventoryClick(event);
        } else if (daStructure instanceof DATable daTable) {
            daTable.handleInventoryClick(event);
        } else if (daStructure instanceof DACrafter daCrafter) {
            daCrafter.handleInventoryClick(event);
        } else if (InventoryType.FURNACE.equals(event.getInventory().getType())) {
            this.handelFurnace(event);
        } else if (InventoryType.WORKBENCH.equals(event.getInventory().getType())) {
            this.handelWorkbench(event);
        } else if (InventoryType.CRAFTING.equals(event.getInventory().getType())) {
            this.handelCrafting(event);
        }
    }

    private void handelWorkbench(InventoryClickEvent event) {
        if (DAConfig.resetItemCrafting) {
            if (event.getSlot() >= 1 && event.getSlot() <= 9) {
                DAUtil.setSlotDefaultItem(event, event.getSlot());
            }
        }
    }

    private void handelCrafting(InventoryClickEvent event) {
        if (DAConfig.resetItemCrafting) {
            if (event.getSlot() >= 1 && event.getSlot() <= 4) {
                DAUtil.setSlotDefaultItem(event, event.getSlot());
            }
        }
    }

    private void handelFurnace(InventoryClickEvent event) {
        if (DAConfig.resetItemSmelting && event.getSlot() == 0) {
            DAUtil.setSlotDefaultItem(event, 0);
        }
    }

}
