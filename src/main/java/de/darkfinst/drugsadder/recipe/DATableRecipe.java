package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import de.darkfinst.drugsadder.utils.Pair;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DATableRecipe extends DARecipe {

    /**
     * Which Tables currently process the recipe and in which state they are
     * <p>
     * {@code Map<Table, <State, TaskID>>}
     */
    public final Map<DATable, Pair<Integer, Integer>> inProcess = new HashMap<>();

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
    private final DAItem filterTwo;
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
    private final DAItem fuelTwo;

    /**
     * The first material
     */
    private final DAItem materialOne;
    /**
     * The second material
     */
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

    /**
     * Starts the process of the recipe
     *
     * @param daTable          The table to start the process on
     * @param hasSecondProcess If the recipe has a second process
     */
    public void startProcess(DATable daTable, boolean hasSecondProcess) {
        ProcessMaterialOne processMaterialOne = new ProcessMaterialOne(daTable, this, hasSecondProcess, 0);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialOne);
        this.inProcess.put(daTable, Pair.of(0, task.getTaskId()));
    }

    /**
     * Starts the second process of the recipe (only if it has one)
     *
     * @param daTable The table to start the process on
     */
    public void startSecondProcess(DATable daTable) {
        ProcessMaterialTwo processMaterialTwo = new ProcessMaterialTwo(daTable, this, 4);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialTwo);
        this.inProcess.put(daTable, Pair.of(0, task.getTaskId()));
    }

    //TODO: After Restart

    /**
     * Restarts the process of the recipe
     *
     * @param daTable The table to restart the process on
     * @param state   The state to restart the process on
     */
    public void restartProcess(DATable daTable, int state) {
        if (state >= 0 && state < 4) {
            ProcessMaterialOne processMaterialOne = new ProcessMaterialOne(daTable, this, true, state);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialOne);
            this.inProcess.put(daTable, Pair.of(state, task.getTaskId()));
        } else if (state >= 4 && state < 8) {
            ProcessMaterialTwo processMaterialTwo = new ProcessMaterialTwo(daTable, this, state);
            BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialTwo);
            this.inProcess.put(daTable, Pair.of(state, task.getTaskId()));
        } else {
            DA.log.errorLog("Invalid State: " + state);
        }

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
        daTable.getInventory().setItem(daTable.getResultSlot(), this.getResult().getItemStack());
        this.inProcess.remove(daTable);
        if (daTable.isThisRecipe(this)) {
            daTable.startRecipe(null, this);
        }
    }

    /**
     * Cancels the process of the recipe
     *
     * @param daTable The table to cancel the process on
     * @param reason  The reason why the process was canceled
     * @param isAsync If the method is called async
     */
    public void cancelProcess(DATable daTable, String reason, boolean isAsync) {
        if (this.inProcess.containsKey(daTable)) {
            Bukkit.getScheduler().cancelTask(this.inProcess.get(daTable).getSecond());
            DA.log.log("Recipe " + this.getRecipeNamedID() + " was canceled because " + reason, isAsync);
            this.updateView(daTable, 0, isAsync);
            this.inProcess.remove(daTable);
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
    //TODO: Ad custom time for processing
    public static class ProcessMaterialOne implements Runnable {

        private final int state;
        private final DATable daTable;
        private final DATableRecipe recipe;
        private final boolean hasSecondProcess;

        public ProcessMaterialOne(DATable daTable, DATableRecipe recipe, boolean hasSecondProcess, int state) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
            this.hasSecondProcess = hasSecondProcess;
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
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, hasSecondProcess, 1), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(1, task.getTaskId()));
                } else if (state == 1) {
                    recipe.updateView(daTable, 2, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, hasSecondProcess, 2), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(2, task.getTaskId()));
                } else if (state == 2) {
                    recipe.updateView(daTable, 3, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialOne(daTable, recipe, hasSecondProcess, 3), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(3, task.getTaskId()));
                } else if (state == 3) {
                    recipe.updateView(daTable, 4, true);
                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[0]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 3", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getMaterialSlots()[0]).getAmount() - recipe.getMaterials()[0].getAmount()));
                    if (recipe.consumeFilterOne) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[0]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[0]).getAmount() - 1));
                    }
                    if (this.hasSecondProcess) {
                        this.recipe.startSecondProcess(this.daTable);
                    } else {
                        this.recipe.finishProcess(this.daTable);
                    }
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
    //TODO: Ad custom time for processing
    public static class ProcessMaterialTwo implements Runnable {

        private final int state;
        private final DATable daTable;
        private final DATableRecipe recipe;

        public ProcessMaterialTwo(DATable daTable, DATableRecipe recipe, int state) {
            this.daTable = daTable;
            this.recipe = recipe;
            this.state = state;
        }

        @Override
        public void run() {
            try {
                if (state == 4) {
                    recipe.updateView(daTable, 5, true);
                    if (daTable.getInventory().getItem(daTable.getFuelSlots()[1]) == null || daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() < recipe.getFuelTwo().getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 4", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFuelSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFuelSlots()[1]).getAmount() - recipe.getFuelTwo().getAmount()));
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, 5), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(5, task.getTaskId()));
                } else if (state == 5) {
                    recipe.updateView(daTable, 6, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, 6), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(6, task.getTaskId()));
                } else if (state == 6) {
                    recipe.updateView(daTable, 7, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterialTwo(daTable, recipe, 7), (10 * 20));
                    recipe.inProcess.put(daTable, Pair.of(7, task.getTaskId()));
                } else if (state == 7) {
                    if (daTable.getInventory().getItem(daTable.getMaterialSlots()[1]) == null || daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).getAmount() < recipe.getMaterials()[0].getAmount()) {
                        recipe.cancelProcess(daTable, "Not enough Materials  - State 5", true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getMaterialSlots()[1]).getAmount() - recipe.getMaterials()[1].getAmount()));
                    if (recipe.consumeFilterTwo) {
                        Bukkit.getScheduler().runTask(DA.getInstance, () -> daTable.getInventory().getItem(daTable.getFilterSlots()[1]).setAmount(daTable.getInventory().getItem(daTable.getFilterSlots()[1]).getAmount() - 1));
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> recipe.finishProcess(daTable));
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
