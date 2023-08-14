package de.darkfinst.DrugsAdder.constructs.barrel;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class DABarrel implements InventoryHolder {

    @NotNull
    @Override
    public Inventory getInventory() {
        return null;
    }

    public boolean create(Block sign, Player player) {
        DABarrelBody barrelBody = new DABarrelBody(this, sign);
        return barrelBody.isValidBarrel();
    }
}
