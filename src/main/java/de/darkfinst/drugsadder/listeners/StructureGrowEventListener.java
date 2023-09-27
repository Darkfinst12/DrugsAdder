package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * This class handles the {@link StructureGrowEvent}
 */
public class StructureGrowEventListener implements Listener {

    public StructureGrowEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        if (DA.loader.isStructure(event.getLocation().getBlock())) {
            event.setCancelled(true);
        }
    }
}
