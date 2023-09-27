package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

/**
 * This class handles the {@link FurnaceBurnEvent}
 * <br>
 * Currently not used
 */
public class FurnaceBurnEventListener implements Listener {

    public FurnaceBurnEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onFurnaceBurnEvent(FurnaceBurnEvent event) {

    }
}
