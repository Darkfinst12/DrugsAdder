package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryDragEventListener implements Listener {

    public InventoryDragEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        DAStructure daStructure = DA.loader.getStructure(event.getInventory());
        if (daStructure instanceof DATable daTable) {
            daTable.handleInventoryDrag(event);
        } else if (daStructure instanceof DACrafter daCrafter) {
            DA.log.debugLog("InventoryDragEvent - DACrafter");
            daCrafter.handleInventoryDrag(event);
        } else if (InventoryType.FURNACE.equals(event.getInventory().getType())) {
            this.handelFurnace(event);
        } else if (InventoryType.WORKBENCH.equals(event.getInventory().getType())) {
            this.handelWorkbench(event);
        } else if (InventoryType.CRAFTING.equals(event.getInventory().getType())) {
            this.handelCrafting(event);
        }
    }

    private void handelWorkbench(InventoryDragEvent event) {
        if (DAConfig.resetItemCrafting) {
            for (Integer slot : event.getInventorySlots()) {
                if (slot >= 1 && slot <= 9) {
                    DAUtil.setSlotDefaultItem(event, slot);
                }
            }
        }
    }

    private void handelCrafting(InventoryDragEvent event) {
        if (DAConfig.resetItemCrafting) {
            for (Integer slot : event.getInventorySlots()) {
                if (slot >= 1 && slot <= 4) {
                    DAUtil.setSlotDefaultItem(event, slot);
                }
            }
        }
    }

    private void handelFurnace(InventoryDragEvent event) {
        if (DAConfig.resetItemSmelting && event.getInventorySlots().contains(0)) {
            DAUtil.setSlotDefaultItem(event, 0);
        }
    }

}
