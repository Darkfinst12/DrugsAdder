package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakEventListener implements Listener {

    public BlockBreakEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (DA.loader.isStructure(event.getBlock())) {
            DA.loader.unregisterDAStructure(event.getBlock());
        }
    }

}
