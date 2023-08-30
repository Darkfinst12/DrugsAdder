package de.darkfinst.drugsadder.structures.press;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.press.CompressItemsEvent;
import de.darkfinst.drugsadder.api.events.press.PressItemEvent;
import de.darkfinst.drugsadder.api.events.press.UnCompressItemsEvent;
import de.darkfinst.drugsadder.api.events.press.UsePressEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DAPressRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.PistonHead;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class DAPress extends DAStructure {

    private final ConcurrentLinkedDeque<ItemStack> compressedItems = new ConcurrentLinkedDeque<>();
    private Long pressActiveTime = null;

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.press.create")) {
            DAPressBody pressBody = new DAPressBody(this, sign);
            try {
                boolean isValid = pressBody.isValidPress();
                if (isValid) {
                    super.setBody(pressBody);
                    DA.loader.registerDAStructure(this);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public DAPressBody getBody() {
        return (DAPressBody) super.getBody();
    }

    public void usePress(Player player) {
        UsePressEvent usePressEvent = new UsePressEvent(this, player);
        Bukkit.getPluginManager().callEvent(usePressEvent);
        if (usePressEvent.isCancelled()) {
            return;
        }
        if (player.hasPermission("drugsadder.press.use")) {
            try {
                Block block = this.getBody().getPiston();
                Piston piston = (Piston) block.getBlockData();
                Block lever = this.getBody().getLever();
                Powerable leverData = (Powerable) lever.getBlockData();
                if (piston.isExtended()) {
                    this.pressItems();
                    Block head = block.getRelative(piston.getFacing());
                    if (this.dropItems(head)) {
                        leverData.setPowered(false);
                        lever.setBlockData(leverData, false);
                        piston.setExtended(false);
                        block.setBlockData(piston, false);
                        head.setType(Material.AIR);
                        this.pressActiveTime = null;
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1, 1);
                    }
                } else {
                    Block head = block.getRelative(piston.getFacing());
                    if (this.compressItems(head)) {
                        leverData.setPowered(true);
                        lever.setBlockData(leverData, false);
                        head.setType(Material.PISTON_HEAD);
                        PistonHead headData = (PistonHead) head.getBlockData();
                        headData.setFacing(piston.getFacing());
                        head.setBlockData(headData, false);

                        piston.setExtended(true);
                        block.setBlockData(piston, false);
                        this.pressActiveTime = System.currentTimeMillis();
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 1);
                    }
                }
            } catch (Exception e) {
                DA.loader.logException(e);
                DA.loader.unregisterDAStructure(this);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoUse"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    private boolean dropItems(Block block) {
        UnCompressItemsEvent unCompressItemsEvent = new UnCompressItemsEvent(this, this.compressedItems.stream().toList());
        Bukkit.getPluginManager().callEvent(unCompressItemsEvent);
        if (unCompressItemsEvent.isCancelled()) {
            return false;
        }
        for (ItemStack compressedItem : unCompressItemsEvent.getItems()) {
            block.getWorld().dropItemNaturally(block.getLocation(), compressedItem);
        }
        this.compressedItems.clear();
        return true;
    }

    private boolean compressItems(Block block) {
        Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), 0.8, 0.8, 0.8, entity -> entity instanceof Item);
        List<Item> items = new ArrayList<>();
        for (Entity entity : entities) {
            if (entity instanceof Item item) {
                items.add(item);
            }
        }
        CompressItemsEvent compressItemsEvent = new CompressItemsEvent(this, items);
        Bukkit.getPluginManager().callEvent(compressItemsEvent);
        if (compressItemsEvent.isCancelled()) {
            return false;
        } else {
            compressItemsEvent.getItems().forEach(item -> {
                this.compressedItems.add(item.getItemStack());
                item.remove();
            });
        }
        return true;
    }

    private void pressItems() {
        if (this.pressActiveTime == null) {
            DA.loader.errorLog("PressActiveTime is null");
            return;
        }
        List<DAPressRecipe> recipes = DAConfig.daRecipeReader.getPressRecipes();
        ItemStack[] compressedItems = this.compressedItems.toArray(new ItemStack[0]);
        for (DAPressRecipe recipe : recipes) {
            if ((recipe.containsMold(compressedItems))) {
                if (recipe.containsMaterials(compressedItems)) {
                    long duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.pressActiveTime);
                    if (duration < recipe.getDuration()) {
                        return;
                    }
                    if (!this.hasMaterials(recipe, compressedItems)) {
                        DA.loader.debugLog("Press has wrong or not enough materials");
                        return;
                    }
                    PressItemEvent pressItemEvent = new PressItemEvent(this, recipe);
                    Bukkit.getPluginManager().callEvent(pressItemEvent);
                    if (pressItemEvent.isCancelled()) {
                        return;
                    }
                    if (!recipe.isReturnMold()) {
                        this.compressedItems.remove(recipe.getMold().getItemStack());
                    }
                    var fallback = 0;
                    while (this.hasMaterials(recipe, this.compressedItems.toArray(new ItemStack[0])) && fallback < 10) {
                        this.addResult(recipe);
                        fallback++;
                    }
                    return;
                }
            }
        }

    }

    private void addResult(DAPressRecipe recipe) {
        for (ItemStack compressedItem : this.compressedItems) {
            DAItem daItem = recipe.getMaterial(compressedItem);
            if (daItem != null && !DAUtil.matchItems(recipe.getMold().getItemStack(), daItem.getItemStack(), recipe.getMold().getItemMatchTypes())) {
                compressedItem.setAmount(compressedItem.getAmount() - daItem.getAmount());
                if (compressedItem.getAmount() <= 0) {
                    this.compressedItems.remove(compressedItem);
                }
            }
        }
        this.compressedItems.add(recipe.getResult().getItemStack());
    }

    private boolean hasMaterials(DAPressRecipe recipe, ItemStack[] compressedItems) {
        for (ItemStack compressedItem : compressedItems) {
            DAItem daItem = recipe.getMaterial(compressedItem);
            if (daItem == null)
                DA.loader.debugLog("daItem is null");
            if (daItem != null && compressedItem.getAmount() < daItem.getAmount()) {
                DA.loader.debugLog("compressedItem.getAmount() < daItem.getAmount()");
            }
            return daItem == null || compressedItem.getAmount() < daItem.getAmount();
        }
        return true;
    }

}
