package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.structures.press.DAPressBody;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DATable extends DAStructure implements InventoryHolder {

    private final Inventory inventory;

    public DATable() {
        this.inventory = DA.getInstance.getServer().createInventory(this, InventoryType.HOPPER, "Lab Table");
    }

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.table.create")) {
            DATableBody daTableBody = new DATableBody(this, sign);
            try {
                boolean isValid = daTableBody.isValidTable();
                if (isValid) {
                    super.setBody(daTableBody);
                    DA.loader.registerDAStructure(this);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_Created"));
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_NotValid"));
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Table_NoCreate"));
        }
    }

    public void open(Player player) {
        if (player.hasPermission("drugsadder.table.open")) {
            player.openInventory(this.inventory);
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Table_NoOpen"));
        }
    }

    public DATableBody getBody() {
        return (DATableBody) super.getBody();
    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
