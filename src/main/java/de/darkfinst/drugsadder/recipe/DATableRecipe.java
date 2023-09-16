package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

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
        this.setMaterials(this.getMaterialOne(), materialTwo);
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
            boolean otherFinished = daTable.getProcess().getRecipeTwo() != null;
            daTable.getProcess().setState(otherFinished ? 10 : 0);
            ProcessMaterialOne processMaterialOne = new ProcessMaterialOne(daTable, this, daTable.getProcess().getState(), processingTimeOne / 4, otherFinished);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialOne);
            daTable.getProcess().setRecipeOne(this);
            daTable.getProcess().setTaskID(task.getTaskId());
        } else if (side == 1) {
            boolean otherFinished = daTable.getProcess().getRecipeOne() != null;
            daTable.getProcess().setState(otherFinished ? 5 : 0);
            ProcessMaterialTwo processMaterialTwo = new ProcessMaterialTwo(daTable, this, daTable.getProcess().getState(), processingTimeTwo / 4, otherFinished);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialTwo);
            daTable.getProcess().setRecipeTwo(this);
            daTable.getProcess().setTaskID(task.getTaskId());
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
            DAItem result = DAUtil.getItemStackByNamespacedID(DAConfig.suspiciousPotionItem);
            this.addResult(daTable, result);
        }
        daTable.getProcess().reset();
    }

    /**
     * Cancels the process of the recipe
     *
     * @param daTable The table to cancel the process on
     * @param isAsync If the method is called async
     */
    public void cancelProcess(DATable daTable, boolean isAsync) {
        if (daTable.getProcess().isProcessing()) {
            Bukkit.getScheduler().cancelTask(daTable.getProcess().getTaskID());
            this.updateView(daTable, 0, isAsync);
            daTable.getProcess().reset();
            DAItem result = DAUtil.getItemStackByNamespacedID(DAConfig.cancelRecipeItem);
            this.addResult(daTable, result);
        }

    }

    private void addResult(DATable daTable, DAItem result) {
        ItemStack resultItem = result != null ? result.getItemStack() : null;
        if (daTable.getInventory().getItem(daTable.getResultSlot()) == null) {
            daTable.getInventory().setItem(daTable.getResultSlot(), resultItem);
        } else if (resultItem != null) {
            daTable.getWorld().dropItem(daTable.getBody().getSign().getLocation(), resultItem);
        }
    }

    /**
     * Runnable for the first process
     */
    public static class ProcessMaterialOne implements Runnable {

        private final int state;
        private final DATable daTable;
        private final DATableRecipe recipe;

        private final int otherFinished;

        private final double processingTime;

        public ProcessMaterialOne(DATable daTable, DATableRecipe recipe, int state, double processingTime, boolean otherFinished) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
            this.processingTime = processingTime;
            this.otherFinished = otherFinished ? 10 : 0;
        }

        @Override
        public void run() {
            try {
                if (state == this.otherFinished) {
                    int newState = state + 1;
                    if (daTable.getInventory().getItem(daTable.getFuelSlots()[0]) == null || daTable.getInventory().getItem(daTable.getFuelSlots()[0]).getAmount() < recipe.getFuelTwo().getAmount()) {
                        recipe.cancelProcess(daTable, true);
                        return;
                    }
                    recipe.updateView(daTable, newState, true);
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFuelSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getFuelSlots()[0]).getAmount() - recipe.getFuelOne().getAmount()));
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, newState, processingTime, this.otherFinished == 10), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(1);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == (1 + this.otherFinished) || state == (2 + this.otherFinished) || state == (3 + this.otherFinished)) {
                    int newState = state + 1;
                    recipe.updateView(daTable, newState, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, newState, processingTime, this.otherFinished == 10), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(newState);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == (4 + this.otherFinished)) {
                    int newState = state + 1;
                    if (newState == 15) {
                        newState = newState + 5;
                    }
                    recipe.updateView(daTable, newState, true);
                    daTable.getProcess().setState(newState);
                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[0]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() - recipe.getMaterials()[0].getAmount()));
                    if (recipe.consumeFilterOne) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[0]).getAmount() - 1));
                    }
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

        private final int otherFinished;

        public ProcessMaterialTwo(DATable daTable, DATableRecipe recipe, int state, double processingTime, boolean otherFinished) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
            this.processingTime = processingTime;
            this.otherFinished = otherFinished ? 10 : 0;
        }

        @Override
        public void run() {
            try {
                if (state == 0 || state == 5) {
                    int newState = state == 0 ? 6 : 16;
                    recipe.updateView(daTable, newState, true);
                    if (daTable.getInventory().getItem(daTable.getFuelSlots()[1]) == null || daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() < recipe.getFuelTwo().getAmount()) {
                        recipe.cancelProcess(daTable, true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFuelSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() - recipe.getFuelTwo().getAmount()));
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, newState, processingTime, otherFinished == 10), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(6);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == (6 + this.otherFinished) || state == (7 + this.otherFinished) || state == (8 + this.otherFinished)) {
                    int newState = state + 1;
                    recipe.updateView(daTable, newState, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, newState, processingTime, otherFinished == 10), (long) Math.floor(processingTime * 20));
                    daTable.getProcess().setState(newState);
                    daTable.getProcess().setTaskID(task.getTaskId());
                } else if (state == (9 + this.otherFinished)) {
                    int newState = state + 1;
                    recipe.updateView(daTable, newState, true);
                    daTable.getProcess().setState(newState);

                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[1]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> {
                        ItemStack invItem = daTable.getInventory().getItem(daTable.getMaterialSlots()[1]);
                        int newAmount = invItem.getAmount() - recipe.getMaterials()[1].getAmount();
                        invItem.setAmount(newAmount);
                        daTable.getInventory().setItem(daTable.getMaterialSlots()[1], invItem);
                    });
                    if (recipe.consumeFilterTwo) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[1]).getAmount() - 1));
                    }
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
                DA.loader.debugLog("Updating view of " + viewer.getUniqueId() + " to " + state, isAsync);
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
