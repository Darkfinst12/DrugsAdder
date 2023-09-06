package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.barrel.BarrelProcessMaterialsEvent;
import de.darkfinst.drugsadder.exceptions.Barrel.BarrelException;
import de.darkfinst.drugsadder.exceptions.Barrel.NotEnoughMaterialsException;
import de.darkfinst.drugsadder.exceptions.Barrel.NotEnoughTimePassedException;
import de.darkfinst.drugsadder.exceptions.Barrel.TooMuchTimePassedException;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Getter
public class DABarrelRecipe extends DARecipe {

    private final long processTime;
    private final long processOverdueAcceptance;

    public DABarrelRecipe(String namedID, RecipeType recipeType, long processTime, long processOverdueAcceptance, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.processTime = processTime;
        this.processOverdueAcceptance = processOverdueAcceptance;
    }

    public void processMaterials(DABarrel barrel) {
        List<ItemStack> lItems = this.getInventoryContents(barrel);
        if (this.hasMaterials(lItems.toArray(new ItemStack[0]))) {
            long current = System.currentTimeMillis();
            BarrelProcessMaterialsEvent event = new BarrelProcessMaterialsEvent(this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            try {
                int fallback = 0;
                while (this.hasMaterials(this.getInventoryContents(barrel).toArray(new ItemStack[0]))) {
                    if (fallback >= 100) {
                        break;
                    }
                    this.addResult(barrel, current);
                    fallback++;
                }
            } catch (BarrelException ignored) {
                //No Materials need to be removed
            }
        }
    }

    @NotNull
    private List<ItemStack> getInventoryContents(DABarrel barrel) {
        List<ItemStack> lItems = new ArrayList<>();
        for (ItemStack storageContent : barrel.getInventory().getContents().clone()) {
            if (storageContent != null && !Material.AIR.equals(storageContent.getType())) {
                ItemStack stack = storageContent.clone();
                barrel.removeTimeStamp(stack);
                lItems.add(stack);
            } else {
                lItems.add(storageContent);
            }
        }
        return lItems;
    }

    private void addResult(DABarrel barrel, long current) throws NotEnoughMaterialsException, NotEnoughTimePassedException, TooMuchTimePassedException {
        Map<Integer, DAItem> modifiedSlots = new HashMap<>();
        for (DAItem material : this.getMaterials()) {
            List<ItemStack> lItems = this.getInventoryContents(barrel);
            for (int i = 0; i < lItems.size(); i++) {
                if (DAUtil.matchItems(material.getItemStack(), lItems.get(i), material.getItemMatchTypes())) {
                    long passedTime = current - barrel.getTimeStamp(barrel.getInventory().getItem(i));
                    long passedSeconds = TimeUnit.MILLISECONDS.toSeconds(passedTime);

                    if (material.getAmount() > lItems.get(i).getAmount()) {
                        throw new NotEnoughMaterialsException();
                    }
                    if (passedSeconds < TimeUnit.MINUTES.toSeconds(this.getProcessTime())) {
                        throw new NotEnoughTimePassedException();
                    }

                    if (passedSeconds > TimeUnit.MINUTES.toSeconds(this.getProcessTime()) + TimeUnit.MINUTES.toSeconds(this.getProcessOverdueAcceptance())) {
                        throw new TooMuchTimePassedException();
                    }
                    modifiedSlots.put(i, material);
                }
            }
        }
        if (!modifiedSlots.isEmpty()) {
            for (Integer slotBarrel : modifiedSlots.keySet()) {
                DAItem material = modifiedSlots.get(slotBarrel);
                ItemStack itemStack = barrel.getInventory().getItem(slotBarrel);
                if (itemStack != null && !itemStack.getType().equals(Material.AIR)) {
                    int newAmount = itemStack.getAmount() - material.getAmount();
                    if (newAmount <= 0) {
                        barrel.getInventory().clear(slotBarrel);
                    } else {
                        itemStack.setAmount(newAmount);
                    }
                }
            }
            HashMap<Integer, ItemStack> notAdded = barrel.getInventory().addItem(this.getResult().getItemStack());
            if (!notAdded.isEmpty()) {
                for (ItemStack itemStack : notAdded.values()) {
                    barrel.getBody().getWorld().dropItemNaturally(barrel.getBody().getSign().getLocation(), itemStack);
                }
            }
        }
    }

}
