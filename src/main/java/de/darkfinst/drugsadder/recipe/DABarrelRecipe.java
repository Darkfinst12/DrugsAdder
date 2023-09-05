package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.api.events.barrel.BarrelProcessMaterialsEvent;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        if (this.containsMaterials(lItems.toArray(new ItemStack[0]))) {
            long current = System.currentTimeMillis();
            if (!this.hasMaterials(lItems.toArray(new ItemStack[0]))) {
                return;
            }
            BarrelProcessMaterialsEvent event = new BarrelProcessMaterialsEvent(this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            int fallback = 0;
            while (this.hasMaterials(this.getInventoryContents(barrel).toArray(new ItemStack[0]))) {
                if (fallback >= 100) {
                    break;
                }
                this.addResult(barrel, current);
                fallback++;
            }

        }
    }

    @NotNull
    private List<ItemStack> getInventoryContents(DABarrel barrel) {
        return Arrays.asList(barrel.getInventory().getStorageContents());
    }

    private void addResult(DABarrel barrel, long current) {
        for (DAItem material : this.getMaterials()) {
            List<ItemStack> lItems = this.getInventoryContents(barrel);
            for (int i = 0; i < lItems.size(); i++) {
                long passedTime = current - barrel.getTimeStamp(barrel.getInventory().getItem(i));
                long passedSeconds = TimeUnit.MILLISECONDS.toSeconds(passedTime);

                if (passedSeconds > TimeUnit.MINUTES.toSeconds(this.getProcessTime()) && passedSeconds < TimeUnit.MINUTES.toSeconds(this.getProcessTime()) + TimeUnit.MINUTES.toSeconds(this.getProcessOverdueAcceptance())) {
                    continue;
                }
                if (DAUtil.matchItems(material.getItemStack(), lItems.get(i), material.getItemMatchTypes())) {
                    int newAmount = lItems.get(i).getAmount() - material.getAmount();
                    if (newAmount <= 0) {
                        barrel.getInventory().clear(i);
                    } else {
                        lItems.get(i).setAmount(newAmount);
                    }
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
