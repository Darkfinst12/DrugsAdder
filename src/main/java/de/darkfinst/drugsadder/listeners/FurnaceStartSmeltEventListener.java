package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;

/**
 * This class handles the {@link FurnaceStartSmeltEvent}
 * <br>
 * Currently not used
 */
public class FurnaceStartSmeltEventListener implements Listener {

    public FurnaceStartSmeltEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onFurnaceStartSmeltEvent(FurnaceStartSmeltEvent event) {

    }
}
