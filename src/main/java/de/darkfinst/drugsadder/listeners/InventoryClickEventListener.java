package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

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
        } else if (InventoryType.FURNACE.equals(event.getInventory().getType())) {
            this.handelFurnace(event);
        } else if (InventoryType.WORKBENCH.equals(event.getInventory().getType())) {
            this.handelCrafting(event);
        }
    }

    private void handelCrafting(InventoryClickEvent event) {
        if (event.getSlot() >= 1 && event.getSlot() <= 9) {
            Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> {
                ItemStack ogItem = event.getInventory().getItem(event.getSlot());
                if (ogItem != null) {
                    ItemStack itemStack = DAUtil.getDefaultItem(ogItem);
                    if (itemStack != null) {
                        itemStack.setAmount(ogItem.getAmount());
                        event.getInventory().setItem(event.getSlot(), itemStack);
                    }
                }
            }, 1L);
        }
    }

    private void handelFurnace(InventoryClickEvent event) {
        if (event.getSlot() == 0) {
            Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> {
                ItemStack ogItem = event.getInventory().getItem(0);
                if (ogItem != null) {
                    ItemStack itemStack = DAUtil.getDefaultItem(ogItem);
                    if (itemStack != null) {
                        itemStack.setAmount(ogItem.getAmount());
                        event.getInventory().setItem(0, itemStack);
                    }
                }
            }, 1L);
        }
    }


}
