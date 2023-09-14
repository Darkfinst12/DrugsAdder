package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ItemsAdderLoadDataEventListener implements Listener {

    public ItemsAdderLoadDataEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onItemsAdderLoadDataEvent(ItemsAdderLoadDataEvent event) {
        if (ItemsAdderLoadDataEvent.Cause.FIRST_LOAD.equals(event.getCause())) {
            DA.loader.log("ItemsAdder has loaded its data, reloading DrugsAdder...");
            DALoader.setIaLoaded(true);
            DA.loader.reloadConfigIA();
        }
    }
}
