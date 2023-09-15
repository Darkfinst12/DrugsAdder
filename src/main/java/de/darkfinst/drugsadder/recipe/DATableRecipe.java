package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.structures.table.DATableProcess;
import de.darkfinst.drugsadder.utils.DAUtil;
import de.darkfinst.drugsadder.utils.Pair;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DATableRecipe extends DARecipe {

    /**
     * The filter for the first material
     */
    private final DAItem filterOne;
    /**
     * If the filter should be consumed
     */
    @Setter
    private boolean consumeFilterOne = false;

    /**
     * The filter for the second material
     */
    private DAItem filterTwo;
    /**
     * If the filter should be consumed
     */
    @Setter
    private boolean consumeFilterTwo = false;

    /**
     * The fuel for the first material
     */
    private final DAItem fuelOne;
    /**
     * The fuel for the second material
     */
    private DAItem fuelTwo;

    /**
     * The first material
     */
    private final DAItem materialOne;
    /**
     * The second material
     */
    private DAItem materialTwo;

    private final double processingTimeOne;

    private double processingTimeTwo;

    public DATableRecipe(String ID, RecipeType recipeType, DAItem result, DAItem filterOne, DAItem fuelOne, DAItem materialOne, double processingTimeOne) {
        super(ID, recipeType, result, materialOne);
        this.filterOne = filterOne;
        this.fuelOne = fuelOne;
        this.materialOne = materialOne;
        this.processingTimeOne = processingTimeOne;
    }

    public void addSecondMaterial(DAItem materialTwo, DAItem filterTwo, DAItem fuelTwo, double processingTimeTwo) {
        this.materialTwo = materialTwo;
        this.filterTwo = filterTwo;
        this.fuelTwo = fuelTwo;
        this.processingTimeTwo = processingTimeTwo;
    }


    /**
     * Starts the process of the recipe
     *
     * @param daTable The table to start the process on
     */
    public void startProcess(DATable daTable, int side) {
        if (side == 0) {
            ProcessMaterialOne processMaterialOne = new ProcessMaterialOne(daTable, this, 0, processingTimeOne / 4);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialOne);
            daTable.getProcess().setRecipeOne(this);
            daTable.getProcess().setTaskID(task.getTaskId());
            daTable.getProcess().setState(0);
        } else if (side == 1) {
            ProcessMaterialTwo processMaterialTwo = new ProcessMaterialTwo(daTable, this, 5, processingTimeTwo / 4);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialTwo);
            daTable.getProcess().setRecipeTwo(this);
            daTable.getProcess().setTaskID(task.getTaskId());
            daTable.getProcess().setState(5);
        }

    }

    /**
     * Restarts the process of the recipe
     *
     * @param daTable The table to restart the process on
     * @param state   The state to restart the process on
     */
    public void restartProcess(DATable daTable, int state) {

    }

    /**
     * Finishes the process of the recipe
     * <b>
     * Sets the result in the result slot of the table and removes the recipe from the inProcess map
     * <p>
     * If enough materials are in the table, the recipe will start again
     *
     * @param daTable The table to finish the process on and to start the recipe again
     */
    public void finishProcess(DATable daTable) {
        this.updateView(daTable, 0, true);
        if (this.equals(daTable.getProcess().getRecipeOne()) && this.equals(daTable.getProcess().getRecipeTwo())) {
            daTable.getInventory().setItem(daTable.getResultSlot(), this.getResult().getItemStack());
        } else {
            //TODO: Add suspicious brew
        }
        daTable.getProcess().reset();
    }

    /**
     * Cancels the process of the recipe
     *
     * @param daTable The table to cancel the process on
     * @param reason  The reason why the process was canceled
     * @param isAsync If the method is called async
     */
    public void cancelProcess(DATable daTable, String reason, boolean isAsync) {
        if (daTable.getProcess().isProcessing()) {
            Bukkit.getScheduler().cancelTask(daTable.getProcess().getTaskID());
            DA.log.log("Recipe " + this.getRecipeNamedID() + " was canceled because " + reason, isAsync);
            this.updateView(daTable, 0, isAsync);
            daTable.getProcess().reset();
            DAItem result = DAUtil.getItemStackByNamespacedID(DAConfig.cancelRecipeItem);
            ItemStack resultItem = result != null ? result.getItemStack() : null;
            if (daTable.getInventory().getItem(daTable.getResultSlot()) == null) {
                daTable.getInventory().setItem(daTable.getResultSlot(), resultItem);
            } else if (resultItem != null) {
                daTable.getWorld().dropItem(daTable.getBody().getSign().getLocation(), resultItem);
            }

        }

    }

    /**
     * Runnable for the first process
     */
    public static class ProcessMaterialOne implements Runnable {

        private final int state;
        private final DATable daTable;
        private final DATableRecipe recipe;

        private final double processingTime;

        public ProcessMaterialOne(DATable daTable, DATableRecipe recipe, int state, double processingTime) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
            this.processingTime = processingTime;
        }

        @Override
        public void run() {
            try {
                if (state == 0) {
                    if (daTable.getInventory().getItem(daTable.getFuelSlots()[0]) == null || daTable.getInventory().getItem(daTable.getFuelSlots()[0]).getAmount() < recipe.getFuelTwo().getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials - State 0", true);
                        return;
                    }
                    recipe.updateView(daTable, 1, true);
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFuelSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getFuelSlots()[0]).getAmount() - recipe.getFuelOne().getAmount()));
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, 1, processingTime), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(1);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state > 0 && state < 4) {
                    int newState = state + 1;
                    recipe.updateView(daTable, newState, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, newState, processingTime), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(newState);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == 4) {
                    if (daTable.getProcess().getRecipeTwo() != null) {
                        recipe.updateView(daTable, 10, true);
                    } else {
                        recipe.updateView(daTable, 5, true);
                    }
                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[0]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 3", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() - recipe.getMaterials()[0].getAmount()));
                    if (recipe.consumeFilterOne) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[0]).getAmount() - 1));
                    }
                    daTable.getProcess().setState(5);
                    daTable.getProcess().setTaskID(-1);
                } else {
                    DA.log.errorLog("Invalid State: " + state);
                }
            } catch (Exception e) {
                DA.log.logException(e, true);
            }

        }
    }

    /**
     * Runnable for the second process
     */
    public static class ProcessMaterialTwo implements Runnable {

        private final int state;
        private final DATable daTable;
        private final DATableRecipe recipe;
        private final double processingTime;

        public ProcessMaterialTwo(DATable daTable, DATableRecipe recipe, int state, double processingTime) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
            this.processingTime = processingTime;
        }

        @Override
        public void run() {
            try {
                if (state == 5) {
                    recipe.updateView(daTable, 6, true);
                    if (daTable.getInventory().getItem(daTable.getFuelSlots()[1]) == null || daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() < recipe.getFuelTwo().getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 4", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFuelSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() - recipe.getFuelTwo().getAmount()));
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, 5, processingTime), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(6);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state > 5 && state < 9) {
                    int newState = state + 1;
                    recipe.updateView(daTable, newState, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, newState, processingTime), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(newState);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == 9) {
                    if (daTable.getProcess().getRecipeOne() != null) {
                        recipe.updateView(daTable, 10, true);
                    } else {
                        recipe.updateView(daTable, 9, true);
                    }
                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[1]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 5", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).getAmount() - recipe.getMaterials()[1].getAmount()));
                    if (recipe.consumeFilterTwo) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[1]).getAmount() - 1));
                    }
                    daTable.getProcess().setState(9);
                    daTable.getProcess().setTaskID(-1);
                } else {
                    DA.log.errorLog("Invalid State: " + state);
                }
            } catch (Exception e) {
                DA.log.logException(e, true);
            }
        }
    }

    /**
     * Updates the view of the table
     *
     * @param daTable The table to update
     * @param state   The state to update to
     */
    public void updateView(DATable daTable, int state, boolean isAsync) {
        for (HumanEntity viewer : daTable.getInventory().getViewers()) {
            try {
                InventoryView inventoryView = viewer.getOpenInventory();
                inventoryView.setTitle(daTable.getTitle(state));
            } catch (Exception e) {
                DA.log.logException(e, isAsync);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DATableRecipe")
                .replace("}", "") +
                ", filterOne=" + filterOne +
                ", consumeFilterOne=" + consumeFilterOne +
                ", filterTwo=" + filterTwo +
                ", consumeFilterTwo=" + consumeFilterTwo +
                ", fuelOne=" + fuelOne +
                ", fuelTwo=" + fuelTwo +
                ", materialOne=" + materialOne +
                ", materialTwo=" + materialTwo +
                '}';
    }


}
