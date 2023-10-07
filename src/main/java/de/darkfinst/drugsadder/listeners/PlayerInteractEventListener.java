package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.api.events.drug.DrugConsumeEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * This class handles the {@link org.bukkit.event.player.PlayerInteractEvent}
 */
public class PlayerInteractEventListener implements Listener {

    public PlayerInteractEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && DA.loader.isStructure(event.getClickedBlock()) && EquipmentSlot.HAND.equals(event.getHand())) {
            if (Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
                return;
            }
            if (Action.RIGHT_CLICK_BLOCK.equals(event.getAction()) && event.getPlayer().isSneaking() && !Material.AIR.equals(event.getPlayer().getInventory().getItem(event.getHand()).getType())) {
                return;
            }
            event.setCancelled(true);
            DA.loader.openStructure(event.getClickedBlock(), event.getPlayer());
        }
        if (Action.PHYSICAL.equals(event.getAction())) {
            DAPlant.handelChange(event.getClickedBlock());
        }
        if (Action.RIGHT_CLICK_AIR.equals(event.getAction()) && EquipmentSlot.HAND.equals(event.getHand())) {
            this.checkForDrug(event);
        }

    }

    private void checkForDrug(PlayerInteractEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (!item.getType().isEdible()) {
            DADrug daDrug = DAConfig.drugReader.getDrug(item);
            if (daDrug != null) {
                DrugConsumeEvent drugConsumeEvent = new DrugConsumeEvent(daDrug);
                Bukkit.getPluginManager().callEvent(drugConsumeEvent);
                if (!drugConsumeEvent.isCancelled()) {
                    daDrug.consume(event.getPlayer());
                    if (!GameMode.CREATIVE.equals(event.getPlayer().getGameMode())) {
                        daDrug.checkForDurability(item);
                    }
                }

            }
        }
    }

}
