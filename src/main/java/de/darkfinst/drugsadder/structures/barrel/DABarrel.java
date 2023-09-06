package de.darkfinst.drugsadder.structures.barrel;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DABarrelRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;

public class DABarrel extends DAStructure implements InventoryHolder {

    private final Inventory inventory;

    public DABarrel() {
        this.inventory = DA.getInstance.getServer().createInventory(this, 9, DA.loader.getTranslation("Barrel", "Structure_Name_Barrel"));
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

    public void open(Player player, Block block) {
        if (Material.SPRUCE_TRAPDOOR.equals(block.getType())) {
            return;
        }
        if (player.hasPermission("drugsadder.barrel.open")) {
            this.processMaterials();
            player.openInventory(this.inventory);
            player.playSound(player.getLocation(), Sound.BLOCK_BARREL_OPEN, 100, 1);
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Barrel_NoOpen"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    private void processMaterials() {
        for (DABarrelRecipe barrelRecipe : DAConfig.daRecipeReader.getBarrelRecipes()) {
            barrelRecipe.processMaterials(this);
        }
    }

    public DABarrelBody getBody() {
        return (DABarrelBody) super.getBody();
    }

    public void handleInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        player.playSound(player.getLocation(), Sound.BLOCK_BARREL_CLOSE, 100, 1);
        for (ItemStack itemStack : event.getInventory().getContents()) {
            this.addTimeStamp(itemStack);
        }
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (this.inventory.equals(event.getClickedInventory())) {
            this.removeTimeStamp(event.getCursor());
            this.removeTimeStamp(event.getCurrentItem());
        }
    }

    public void addTimeStamp(ItemStack itemStack) {
        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
            NamespacedKey key = new NamespacedKey(DA.getInstance, "brew_timestamp");
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (!itemMeta.getPersistentDataContainer().has(key, PersistentDataType.LONG)) {
                itemMeta.getPersistentDataContainer().set(key, PersistentDataType.LONG, System.currentTimeMillis());
                itemStack.setItemMeta(itemMeta);
            }
        }
    }

    public long getTimeStamp(ItemStack itemStack) {
        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
            NamespacedKey key = new NamespacedKey(DA.getInstance, "brew_timestamp");
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta.getPersistentDataContainer().has(key, PersistentDataType.LONG)) {
                try {
                    return itemMeta.getPersistentDataContainer().get(key, PersistentDataType.LONG);
                } catch (Exception e) {
                    DA.loader.logException(e);
                    return System.currentTimeMillis();
                }
            }
        }
        return System.currentTimeMillis();
    }

    public void removeTimeStamp(ItemStack itemStack) {
        if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
            NamespacedKey key = new NamespacedKey(DA.getInstance, "brew_timestamp");
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.getPersistentDataContainer().remove(key);
            itemStack.setItemMeta(itemMeta);
        }
    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public void destroyInventory() {
        for (ItemStack content : this.inventory.getContents()) {
            if (content != null && !content.getType().equals(Material.AIR)) {
                this.getBody().getWorld().dropItemNaturally(this.getBody().getSign().getLocation(), content);
            }
        }
    }

    @Override
    public boolean hasInventory() {
        return true;
    }

}
