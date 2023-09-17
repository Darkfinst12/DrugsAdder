package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.utils.DAUtil;
import lol.simeon.bpmcalculator.BPMAPI;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class DACrafterRecipe extends DARecipe {

    /**
     * The shape of the recipe
     */
    private final List<String> shape = new ArrayList<>(5);
    /**
     * The keys of the shape
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();
    /**
     * Whether the recipe is shapeless or not
     */
    @Setter
    private boolean isShapeless = false;

    /**
     * The processing time of the recipe
     */
    private final double processingTime;

    /**
     * The required players for the recipe
     */
    private final int requiredPlayers;

    public DACrafterRecipe(String ID, RecipeType recipeType, DAItem result, double processingTime, int requiredPlayers, DAItem... materials) {
        super(ID, recipeType, result, materials);
        this.processingTime = processingTime;
        this.requiredPlayers = requiredPlayers;
    }

    public void setShape(String... shape) {
        this.shape.clear();
        this.shape.addAll(Arrays.asList(shape));
    }

    public void setShapeKeys(@NotNull Map<String, DAItem> shapeKeys) {
        this.shapeKeys.clear();
        this.shapeKeys.putAll(shapeKeys);
    }

    public boolean matchMaterials(Map<Integer, ItemStack> matrix) {
        try {
            if (isShapeless) {
                return this.hasMaterials(matrix.values().toArray(new ItemStack[0]));
            } else {
                return this.matchShape(matrix);
            }
        } catch (IllegalArgumentException e) {
            DA.log.debugLog(e.getMessage());
            return false;
        }
    }

    public boolean matchShape(Map<Integer, ItemStack> matrix) throws IllegalArgumentException {
        if (this.isShapeless) {
            throw new IllegalArgumentException("Recipe is shapeless");
        }
        if (matrix.size() != 25) {
            throw new IllegalArgumentException("Matrix length must be 25");
        }

        for (int i = 0; i < 5; i++) {
            String row = this.shape.get(i);
            for (int j = 0; j < 5; j++) {
                String key = String.valueOf(row.charAt(j));
                if (!key.equals(" ")) {
                    DAItem item = this.shapeKeys.get(key);
                    int slot = i * 9 + j;
                    if (item == null) {
                        continue;
                    }
                    if (!DAUtil.matchItems(item.getItemStack(), matrix.get(slot), item.getItemMatchTypes()) || matrix.get(slot).getAmount() < item.getItemStack().getAmount()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void executeShape(DACrafter daCrafter) {
        var matrix = daCrafter.getContentMap();
        if (this.isShapeless) {
            throw new IllegalArgumentException("Recipe is shapeless");
        }
        if (matrix.size() != 25) {
            throw new IllegalArgumentException("Matrix length must be 25");
        }
        for (int i = 0; i < 5; i++) {
            String row = this.shape.get(i);
            for (int j = 0; j < 5; j++) {
                String key = String.valueOf(row.charAt(j));
                if (!key.equals(" ")) {
                    DAItem item = this.shapeKeys.get(key);
                    int slot = i * 9 + j;
                    if (item == null) {
                        DA.log.debugLog("Item is null at " + slot);
                        return;
                    }
                    if (DAUtil.matchItems(item.getItemStack(), matrix.get(slot), item.getItemMatchTypes()) && matrix.get(slot).getAmount() >= item.getItemStack().getAmount()) {
                        int newAmount = matrix.get(slot).getAmount() - item.getItemStack().getAmount();
                        if (newAmount <= 0) {
                            daCrafter.getInventory().setItem(slot, null);
                        } else {
                            daCrafter.getInventory().getItem(slot).setAmount(newAmount);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
        this.addResult(daCrafter, this.getResult());
    }

    public void startProcess(DACrafter daCrafter) {
        daCrafter.getProcess().setRecipe(this);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, new ProcessMaterials(0, daCrafter, this, this.processingTime / 7));
        daCrafter.getProcess().setTaskID(task.getTaskId());
        daCrafter.getProcess().setState(0);
    }

    /**
     * Finishes the process of the recipe
     * <b>
     * Sets the result in the result slot of the table and removes the recipe from the inProcess map
     * <p>
     * If enough materials are in the table, the recipe will start again
     *
     * @param daCrafter The table to finish the process on and to start the recipe again
     */
    public void finishProcess(DACrafter daCrafter, boolean isAsync) {
        this.updateView(daCrafter, 0, isAsync);
        daCrafter.getInventory().setItem(daCrafter.getResultSlot(), this.getResult().getItemStack());
        daCrafter.getProcess().reset();
    }

    /**
     * Cancels the process of the recipe
     *
     * @param daCrafter The table to cancel the process on
     * @param isAsync   If the method is called async
     */
    public void cancelProcess(DACrafter daCrafter, boolean isAsync) {
        if (daCrafter.getProcess().isProcessing()) {
            Bukkit.getScheduler().cancelTask(daCrafter.getProcess().getTaskID());
            this.updateView(daCrafter, 0, isAsync);
            daCrafter.getProcess().reset();
        }

    }

    private void addResult(DACrafter daCrafter, DAItem result) {
        ItemStack resultItem = result != null ? result.getItemStack() : null;
        if (daCrafter.getInventory().getItem(daCrafter.getResultSlot()) == null) {
            daCrafter.getInventory().setItem(daCrafter.getResultSlot(), resultItem);
        } else if (resultItem != null) {
            daCrafter.getWorld().dropItem(daCrafter.getBody().getSign().getLocation(), resultItem);
        }
    }

    /**
     * Updates the view of the table
     *
     * @param dacRafter The table to update
     * @param state     The state to update to
     */
    public void updateView(DACrafter dacRafter, int state, boolean isAsync) {
        for (HumanEntity viewer : dacRafter.getInventory().getViewers()) {
            try {
                InventoryView inventoryView = viewer.getOpenInventory();
                inventoryView.setTitle(dacRafter.getTitle(state));
            } catch (Exception e) {
                DA.log.logException(e, isAsync);
            }
        }
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DACrafterRecipe").replace("}", "") + ", shape=" + shape + ", shapeKeys=" + shapeKeys + ", isShapeless=" + isShapeless + "}";
    }

    public static class ProcessMaterials implements Runnable {

        private final int state;
        private final DACrafter daCrafter;
        private final DACrafterRecipe recipe;
        private final double processingTime;

        public ProcessMaterials(int state, DACrafter daCrafter, DACrafterRecipe recipe, double processingTime) {
            this.state = state;
            this.daCrafter = daCrafter;
            this.recipe = recipe;
            this.processingTime = processingTime;
        }

        @Override
        public void run() {
            try {
                if (state >= 0 && state < 7) {
                    int newState = state + 1;
                    recipe.updateView(daCrafter, newState, true);
                    BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new ProcessMaterials(newState, daCrafter, recipe, processingTime), (long) Math.floor(processingTime * 20));
                    daCrafter.getProcess().setState(newState);
                    daCrafter.getProcess().setTaskID(task.getTaskId());
                } else if (state == 7) {
                    int newState = 0;
                    recipe.updateView(daCrafter, newState, true);
                    if (!recipe.matchMaterials(daCrafter.getContentMap())) {
                        recipe.cancelProcess(daCrafter, true);
                        return;
                    }
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> {
                        try {
                            recipe.executeShape(daCrafter);
                        } catch (Exception e) {
                            DA.log.logException(e, false);
                        }
                    });
                    daCrafter.getProcess().finish(daCrafter, true);
                    daCrafter.getProcess().setTaskID(-1);
                } else {
                    DA.log.errorLog("Invalid State: " + state);
                }
            } catch (Exception e) {
                DA.log.logException(e, true);
            }
        }
    }
}
