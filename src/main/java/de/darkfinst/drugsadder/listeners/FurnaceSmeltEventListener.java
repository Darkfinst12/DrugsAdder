package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

/**
 * This class handles the {@link FurnaceSmeltEvent}
 * <br>
 * Currently not used
 */
public class FurnaceSmeltEventListener implements Listener {

    public FurnaceSmeltEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onFurnaceSmeltEvent(FurnaceSmeltEvent event) {

    }
}
