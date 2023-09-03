package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.api.events.drug.DrugConsumeEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerItemConsumeEventListener implements Listener {

    public PlayerItemConsumeEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        DADrug daDrug = DAConfig.drugReader.getDrug(event.getItem());
        if (daDrug != null) {
            DrugConsumeEvent drugConsumeEvent = new DrugConsumeEvent(daDrug);
            Bukkit.getPluginManager().callEvent(drugConsumeEvent);
            if (!drugConsumeEvent.isCancelled()) {
                daDrug.consume(event.getPlayer());
            }
        }
    }
}
