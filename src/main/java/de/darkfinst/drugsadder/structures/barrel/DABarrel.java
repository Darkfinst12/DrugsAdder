package de.darkfinst.drugsadder.structures.barrel;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DABarrelRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class DABarrel extends DAStructure implements InventoryHolder {

    private final Inventory inventory;

    public DABarrel() {
        this.inventory = DA.getInstance.getServer().createInventory(this, 9, DA.loader.getTranslation("Barrel", "Structure_Name_Barrel"));
    }

    /**
     * Creates a barrel
     * <p>
     * It checks if the player has the permission to create a barrel and if the barrel is valid
     *
     * @param sign   The sign of the barrel
     * @param player The player who created the barrel
     */
    public void create(Block sign, Player player) throws RegisterStructureException {
        if (player.hasPermission("drugsadder.barrel.create")) {
            DABarrelBody barrelBody = new DABarrelBody(this, sign);
            try {
                boolean isValid = barrelBody.isValidBarrel();
                if (isValid) {
                    super.setBody(barrelBody);
                    boolean success = DA.loader.registerDAStructure(this, false);
                    if (success) {
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Barrel_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                    }
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Barrel_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Barrel_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    /**
     * Creates a barrel
     * <p>
     * It checks if the barrel is valid
     *
     * @param sign    The sign of the barrel
     * @param isAsync If the barrel should be created, async
     * @return True if the barrel was successfully created and registered
     * @throws ValidateStructureException If the barrel is not valid
     */
    public boolean create(Block sign, boolean isAsync) throws ValidateStructureException, RegisterStructureException {
        DABarrelBody barrelBody = new DABarrelBody(this, sign);
        boolean isValid = barrelBody.isValidBarrel();
        if (isValid) {
            super.setBody(barrelBody);
            return DA.loader.registerDAStructure(this, isAsync);
        }
        return false;
    }

    /**
     * Opens the barrel inventory for the player
     * <p>
     * It checks if the player has the permission to open a barrel and if it is the right block to open
     * Allowed blocks: BARREL and SIGN
     *
     * @param player The player who opens the barrel
     * @param block  The block of the barrel
     */
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
