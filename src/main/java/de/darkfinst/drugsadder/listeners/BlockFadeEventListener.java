package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

/**
 * This class handles the {@link org.bukkit.event.block.BlockFadeEvent}
 */
public class BlockFadeEventListener implements Listener {

    public BlockFadeEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        DAPlant.handelChange(event.getBlock());
    }
}
