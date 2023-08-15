package de.darkfinst.DrugsAdder.listeners;

import de.darkfinst.DrugsAdder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractEventListener implements Listener {

    public PlayerInteractEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && DA.loader.isStructure(event.getClickedBlock())) {
            event.setCancelled(true);
            DA.loader.openStructure(event.getClickedBlock(), event.getPlayer());
        }
    }

}
