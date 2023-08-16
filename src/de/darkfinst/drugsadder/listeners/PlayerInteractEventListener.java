package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractEventListener implements Listener {

    public PlayerInteractEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && DA.loader.isStructure(event.getClickedBlock()) && EquipmentSlot.HAND.equals(event.getHand()) && event.getItem() == null) {
            event.setCancelled(true);
            DA.loader.openStructure(event.getClickedBlock(), event.getPlayer());
        }
    }

}
