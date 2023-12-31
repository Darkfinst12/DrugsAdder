package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * This class handles the {@link org.bukkit.event.block.BlockBreakEvent}
 */
public class BlockBreakEventListener implements Listener {

    public BlockBreakEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        if (DA.loader.isPlant(event.getBlock()) && !event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            DAPlant daPlant = (DAPlant) DA.loader.getStructure(event.getBlock());
            daPlant.checkHarvest(event.getPlayer());
        } else if (DA.loader.isPlant(event.getBlock()) && event.getPlayer().isSneaking()) {
            event.setDropItems(false);
            DAPlant daPlant = (DAPlant) DA.loader.getStructure(event.getBlock());
            daPlant.destroy(event.getPlayer(), event.getBlock());
        } else if (DA.loader.isStructure(event.getBlock())) {
            DA.loader.unregisterDAStructure(event.getPlayer(), event.getBlock());
        } else if (Material.FARMLAND.equals(event.getBlock().getType()) && DA.loader.isStructure(event.getBlock().getRelative(0, -1, 0))) {
            DA.loader.unregisterDAStructure(event.getPlayer(), event.getBlock().getRelative(0, -1, 0));
        }
    }

}
