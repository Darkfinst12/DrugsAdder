package de.darkfinst.drugsadder.listeners;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import org.bukkit.Bukkit;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * This class handles the {@link org.bukkit.event.block.SignChangeEvent}
 */
public class SignChangeEventListener implements Listener {

    public SignChangeEventListener() {
        Bukkit.getPluginManager().registerEvents(this, DA.getInstance);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        try {
            if (hasBarrelLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
                DABarrel daBarrel = new DABarrel();
                daBarrel.create(event.getBlock(), event.getPlayer());
            } else if (hasPressLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
                DAPress daPress = new DAPress();
                daPress.create(event.getBlock(), event.getPlayer());
            } else if (hasTableLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
                DATable daTable = new DATable();
                daTable.create(event.getBlock(), event.getPlayer());
            } else if (hasCrafterLine(lines) && event.getBlock().getBlockData() instanceof WallSign) {
                DACrafter dacRafter = new DACrafter();
                dacRafter.create(event.getBlock(), event.getPlayer());
            }
        } catch (RegisterStructureException e) {
            DA.loader.msg(event.getPlayer(), DA.loader.languageReader.getComponent("Error_Structures_Register"), DrugsAdderSendMessageEvent.Type.ERROR);
            DA.log.logException(e);
        }

    }

    private boolean hasBarrelLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase(this.getLegacyTranslation("barrel", "Structure_Name_Barrel"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPressLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase(this.getLegacyTranslation("press", "Structure_Name_Press"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTableLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase(this.getLegacyTranslation("table", "Structure_Name_Table"))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCrafterLine(String[] lines) {
        for (String line : lines) {
            if (line.equalsIgnoreCase(this.getLegacyTranslation("crafter", "Structure_Name_Crafter"))) {
                return true;
            }
        }
        return false;
    }

    private String getLegacyTranslation(String fallback, String key) {
        String translation = DA.loader.getTranslation(fallback, key);
        translation = translation.replaceAll("<.*>", "");
        return translation;
    }
}
