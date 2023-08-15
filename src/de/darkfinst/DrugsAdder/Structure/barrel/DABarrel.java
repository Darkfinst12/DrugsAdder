package de.darkfinst.DrugsAdder.Structure.barrel;

import de.darkfinst.DrugsAdder.DA;
import de.darkfinst.DrugsAdder.Structure.DAStructure;
import de.darkfinst.DrugsAdder.Structure.press.DAPressBody;
import de.darkfinst.DrugsAdder.exceptions.ValidateStructureException;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DABarrel extends DAStructure implements InventoryHolder {

    private final Inventory inventory;

    public DABarrel() {
        this.inventory = DA.getInstance.getServer().createInventory(this, 9, "Barrel");
    }

    public boolean create(Block sign, Player player) {
        DABarrelBody barrelBody = new DABarrelBody(this, sign);
        boolean isValid = false;
        try {
            isValid = barrelBody.isValidBarrel();
            if (isValid) {
                super.setBody(barrelBody);
                DA.loader.registerDAStructure(this);
            }
        } catch (ValidateStructureException ignored) {
        }
        return isValid;
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    public DABarrelBody getBody() {
        return (DABarrelBody) super.getBody();
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}
