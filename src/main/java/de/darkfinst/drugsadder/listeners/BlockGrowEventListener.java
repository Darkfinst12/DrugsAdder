package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;

/**
 * This class handles the {@link org.bukkit.event.block.BlockGrowEvent}
 */
public class BlockGrowEventListener implements Listener {

    public BlockGrowEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        if (this.isNearStructure(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean isNearStructure(Block block) {
        return DA.loader.isStructure(block.getRelative(0, 0, 1)) || DA.loader.isStructure(block.getRelative(0, 0, -1)) || DA.loader.isStructure(block.getRelative(1, 0, 0)) || DA.loader.isStructure(block.getRelative(-1, 0, 0));
    }
}
