package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceEventListener implements Listener {

    public BlockPlaceEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (DAConfig.seedReader.isSeed(event.getItemInHand())) {
            DAPlantItem seed = DAConfig.seedReader.getSeed(event.getItemInHand());
            DAPlant plant = new DAPlant(seed, seed.isCrop(), seed.isDestroyOnHarvest(), seed.getGrowthTime(), seed.getDrops());
            Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> plant.create(event.getBlock(), event.getPlayer()), 1L);
        }
        if (DA.loader.isStructure(event.getBlock())) {
            DA.log.unregisterDAStructure(event.getPlayer(), event.getBlock());
        }
    }
}
