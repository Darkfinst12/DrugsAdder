package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

public class BlockGrowEventListener implements Listener {

    public BlockGrowEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        if (DA.loader.isStructure(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
