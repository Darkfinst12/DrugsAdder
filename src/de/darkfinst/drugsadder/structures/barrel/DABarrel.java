package de.darkfinst.drugsadder.structures.barrel;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.structures.DAStructure;
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

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.barrel.create")) {
            DABarrelBody barrelBody = new DABarrelBody(this, sign);
            try {
                boolean isValid = barrelBody.isValidBarrel();
                if (isValid) {
                    super.setBody(barrelBody);
                    DA.loader.registerDAStructure(this, false);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Barrel_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Barrel_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Barrel_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public void create(Block sign, boolean isAsync) throws ValidateStructureException {
        DABarrelBody barrelBody = new DABarrelBody(this, sign);
        boolean isValid = barrelBody.isValidBarrel();
        if (isValid) {
            super.setBody(barrelBody);
            DA.loader.registerDAStructure(this, isAsync);
        }
    }

    public void open(Player player) {
        if (player.hasPermission("drugsadder.barrel.open")) {
            player.openInventory(this.inventory);
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Barrel_NoOpen"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
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
