package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.table.DATable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DATableRecipe extends DARecipe {

    private final Map<DATable, Integer> inProcess = new HashMap<>();

    private final DAItem filterOne;
    private final DAItem filterTwo;

    private final DAItem fuelOne;
    private final DAItem fuelTwo;

    private final DAItem materialOne;
    private final DAItem materialTwo;

    public DATableRecipe(String namedID, RecipeType recipeType, DAItem filterOne, DAItem filterTwo, DAItem fuelOne, DAItem fuelTwo, DAItem result, DAItem... materials) {
        super(namedID, recipeType, result, materials);
        this.filterOne = filterOne;
        this.filterTwo = filterTwo;
        this.fuelOne = fuelOne;
        this.fuelTwo = fuelTwo;
        this.materialOne = materials[0];
        this.materialTwo = materials[1];
    }

    public void startProcess(DATable daTable, boolean hasSecondProcess) {
        ProccessMaterialOne proccessMaterialOne = new ProccessMaterialOne(daTable, this, hasSecondProcess);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, proccessMaterialOne);
        this.inProcess.put(daTable, task.getTaskId());
    }

    public void startSecondProcess(DATable daTable) {
        ProccessMaterialTwo proccessMaterialTwo = new ProccessMaterialTwo(daTable, this);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, proccessMaterialTwo);
        this.inProcess.put(daTable, task.getTaskId());
    }

    public void finishProcess(DATable daTable) {
        daTable.getInventory().setItem(daTable.getResultSlot(), this.getResult().getItemStack());
        this.inProcess.remove(daTable);
    }

    public void cancelProcess(DATable daTable) {
        if (this.inProcess.containsKey(daTable)) {
            Bukkit.getScheduler().cancelTask(this.inProcess.get(daTable));
            this.inProcess.remove(daTable);
            DAItem result = DAConfig.customItemReader.getItemByNamespacedID(DAConfig.cancelRecipeItem);
            ItemStack resultItem = result != null ? result.getItemStack() : null;
            daTable.getInventory().setItem(daTable.getResultSlot(), resultItem);
        }

    }

    //TODO: Refresh Inventory
    public class ProccessMaterialOne implements Runnable {

        private final DATable daTable;
        private final DATableRecipe recipe;
        private final boolean hasSecondProcess;

        public ProccessMaterialOne(DATable daTable, DATableRecipe recipe, boolean hasSecondProcess) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.hasSecondProcess = hasSecondProcess;
        }

        @Override
        public void run() {
            try {
                wait(1000, 0);

                if (this.hasSecondProcess) {
                    this.recipe.startSecondProcess(this.daTable);
                }
            } catch (InterruptedException e) {
                DA.log.logException(e);
            }

        }
    }

    //TODO: Refresh Inventory
    public class ProccessMaterialTwo implements Runnable {

        private final DATable daTable;
        private final DATableRecipe recipe;

        public ProccessMaterialTwo(DATable daTable, DATableRecipe recipe) {
            this.daTable = daTable;
            this.recipe = recipe;
        }

        @Override
        public void run() {
            try {
                wait(1000, 0);
                this.recipe.finishProcess(this.daTable);
            } catch (InterruptedException e) {
                DA.log.logException(e);
            }
        }
    }


}
