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
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.structures.barrel.DABarrelBody;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

public class DAPress extends DAStructure {

    @Getter
    private final ConcurrentLinkedDeque<ItemStack> compressedItems = new ConcurrentLinkedDeque<>();
    private Long pressActiveTime = null;

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.press.create")) {
            DAPressBody pressBody = new DAPressBody(this, sign);
            try {
                boolean isValid = pressBody.isValidPress();
                if (isValid) {
                    super.setBody(pressBody);
                    DA.loader.registerDAStructure(this, false);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Press_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Press_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public void create(Block sign, boolean isAsync) throws ValidateStructureException {
        DAPressBody pressBody = new DAPressBody(this, sign);
        boolean isValid = pressBody.isValidPress();
        if (isValid) {
            super.setBody(pressBody);
            DA.loader.registerDAStructure(this, isAsync);
        }
    }

    public DAPressBody getBody() {
        return (DAPressBody) super.getBody();
    }

    public void usePress(Player player, Block block) {
        if (!Material.LEVER.equals(block.getType())) {
            return;
        }
        UsePressEvent usePressEvent = new UsePressEvent(this, player);
        Bukkit.getPluginManager().callEvent(usePressEvent);
        if (usePressEvent.isCancelled()) {
            return;
        }
        if (player.hasPermission("drugsadder.press.use")) {
            try {
                Block pBlock = this.getBody().getPiston();
                Piston piston = (Piston) pBlock.getBlockData();
                Block lever = this.getBody().getLever();
                Powerable leverData = (Powerable) lever.getBlockData();
                if (piston.isExtended()) {
                    this.pressItems();
                    Block head = pBlock.getRelative(piston.getFacing());
                    if (this.dropItems(head)) {
                        leverData.setPowered(false);
                        lever.setBlockData(leverData, false);
                        piston.setExtended(false);
                        pBlock.setBlockData(piston, false);
                        head.setType(Material.AIR);
                        this.pressActiveTime = null;
                        pBlock.getWorld().playSound(pBlock.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 1, 1);
                    }
                } else {
                    Block head = pBlock.getRelative(piston.getFacing());
                    if (this.compressItems(head)) {
                        leverData.setPowered(true);
                        lever.setBlockData(leverData, false);
                        head.setType(Material.PISTON_HEAD);
                        PistonHead headData = (PistonHead) head.getBlockData();
                        headData.setFacing(piston.getFacing());
                        head.setBlockData(headData, false);

                        piston.setExtended(true);
                        pBlock.setBlockData(piston, false);
                        this.pressActiveTime = System.currentTimeMillis();
                        pBlock.getWorld().playSound(pBlock.getLocation(), Sound.BLOCK_PISTON_EXTEND, 1, 1);
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

    public void addCompressedItem(ItemStack itemStack) {
        this.compressedItems.add(itemStack);
    }

    private void pressItems() {
        if (this.pressActiveTime == null) {
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
                    if (!recipe.hasMaterials(compressedItems)) {
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
                    while (recipe.hasMaterials(this.compressedItems.toArray(new ItemStack[0]))) {
                        if (fallback >= 100) {
                            break;
                        }
                        this.addResult(recipe);
                        fallback++;
                    }
                    return;
                }
            }
        }

    }

    private void addResult(DAPressRecipe recipe) {
        for (DAItem material : recipe.getMaterials()) {
            for (ItemStack compressedItem : this.compressedItems) {
                if (DAUtil.matchItems(material.getItemStack(), compressedItem, material.getItemMatchTypes())) {
                    int newAmount = compressedItem.getAmount() - material.getAmount();
                    if (newAmount <= 0) {
                        this.compressedItems.remove(compressedItem);
                    } else {
                        compressedItem.setAmount(newAmount);
                    }
                    break;
                }
            }
        }
        this.compressedItems.add(recipe.getResult().getItemStack());
    }


    @Override
    public void destroyInventory() {
        Block block = this.getBody().getPiston();
        Block head = block.getRelative(((Piston) block.getBlockData()).getFacing());
        this.dropItems(head);
    }

    @Override
    public boolean hasInventory() {
        return true;
    }
}
