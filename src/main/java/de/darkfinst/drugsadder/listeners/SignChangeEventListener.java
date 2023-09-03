package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.WallSign;
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

        if (hasBarrelLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
            DABarrel daBarrel = new DABarrel();
            daBarrel.create(event.getBlock(), event.getPlayer());

        } else if (hasPressLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
            DAPress daPress = new DAPress();
            daPress.create(event.getBlock(), event.getPlayer());
        } else if (hasTableLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
            DATable daTable = new DATable();
            daTable.create(event.getBlock(), event.getPlayer());
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

    private boolean hasPressLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase("Press")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTableLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase("Table")) {
                return true;
            }
        }
        return false;
    }
}
