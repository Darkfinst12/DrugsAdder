package de.darkfinst.DrugsAdder.Structure.table;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.Structure.DAStructure;
import de.darkfinst.DrugsAdder.Structure.press.DAPressBody;
import de.darkfinst.DrugsAdder.exceptions.ValidateStructureException;
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

    public boolean create(Block sign, Player player) {
        DATableBody daTableBody = new DATableBody(this, sign);
        boolean isValid = false;
        try {
            isValid = daTableBody.isValidTable();
            if (isValid) {
                super.setBody(daTableBody);
                DA.loader.registerDAStructure(this);
            }
        } catch (ValidateStructureException ignored) {
        }
        return isValid;
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
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
