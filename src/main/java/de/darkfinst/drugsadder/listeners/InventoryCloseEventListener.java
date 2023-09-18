package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseEventListener implements Listener {

    public InventoryCloseEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        DAStructure daStructure = DA.loader.getStructure(event.getInventory());
        if (daStructure instanceof DABarrel daBarrel) {
            daBarrel.handleInventoryClose(event);
        } else if (daStructure instanceof DACrafter daCrafter) {
            DA.log.debugLog("InventoryCloseEvent - DACrafter");
            daCrafter.handelInventoryClose(event);
        }
    }


}
