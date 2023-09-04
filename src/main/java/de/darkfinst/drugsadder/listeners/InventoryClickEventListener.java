package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.table.DATable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickEventListener implements Listener {

    public InventoryClickEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
       DAStructure daStructure = DA.loader.getStructure(event.getInventory());
       if(daStructure instanceof DABarrel daBarrel){

       }
       else if(daStructure instanceof DATable daTable){
           daTable.handleInventoryClick(event);
       }
    }


}
