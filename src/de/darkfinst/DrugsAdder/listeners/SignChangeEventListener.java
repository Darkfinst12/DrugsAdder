package de.darkfinst.DrugsAdder.listeners;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.Structure.barrel.DABarrel;
import de.darkfinst.DrugsAdder.Structure.press.DAPress;
import de.darkfinst.DrugsAdder.Structure.table.DATable;
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
            boolean success = daBarrel.create(event.getBlock(), event.getPlayer());
            if (success) {
                DA.loader.msg(event.getPlayer(), "Success - Barrel");
            } else {
                DA.loader.msg(event.getPlayer(), "Fail - Barrel");
            }
        } else if (hasPressLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
            DAPress daPress = new DAPress();
            boolean success = daPress.create(event.getBlock(), event.getPlayer());
            if (success) {
                DA.loader.msg(event.getPlayer(), "Success - Press");
            } else {
                DA.loader.msg(event.getPlayer(), "Fail - Press");
            }
        } else if (hasTableLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
            DATable daTable = new DATable();
            boolean success = daTable.create(event.getBlock(), event.getPlayer());
            if (success) {
                DA.loader.msg(event.getPlayer(), "Success - Table");
            } else {
                DA.loader.msg(event.getPlayer(), "Fail - Table");
            }
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
