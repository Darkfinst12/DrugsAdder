package de.darkfinst.DrugsAdder.listeners;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.constructs.barrel.DABarrel;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeEventListener implements Listener {

    public SignChangeEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();

        if (hasBarrelLine(lines)) {
            DABarrel daBarrel = new DABarrel();
            daBarrel.create(event.getBlock(), event.getPlayer());
        }
    }

    private boolean hasBarrelLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase("Barrel")) {
                return true;
            }
        }
        return false;
    }
}
