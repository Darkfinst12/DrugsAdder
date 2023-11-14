package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class DACrafterRecipe extends DAShapedRecipe {

    /**
     * The shape of the recipe
     */
    private final List<String> shape = new ArrayList<>(5);
    /**
     * The keys of the shape
     */
    private final Map<String, DAItem> shapeKeys = new HashMap<>();
    /**
     * The processing time of the recipe
     */
    private final double processingTime;
    /**
     * The required players for the recipe
     */
    private final int requiredPlayers;
    /**
     * Whether the recipe is shapeless or not
     */
    @Setter
    private boolean isShapeless = false;

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

    public boolean matchMaterials(@NotNull Map<Integer, ItemStack> matrix) {
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

    public boolean matchShape(@NotNull Map<Integer, ItemStack> matrix) throws IllegalArgumentException {
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
                    if (!DAUtil.matchItems(item.getItemStack(), matrix.get(slot), item.getItemMatchTypes()) || matrix.get(slot).getAmount() < item.getAmount()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Executes the shape of the recipe; that means it removes the materials from the table and adds the result
     *
     * @param daCrafter The table to execute the shape on
     * @throws IllegalArgumentException If the recipe is shapeless or the matrix length isn't 25
     */
    public void executeShape(@NotNull DACrafter daCrafter) throws IllegalArgumentException {
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
                        return;
                    }
                    if (DAUtil.matchItems(item.getItemStack(), matrix.get(slot), item.getItemMatchTypes()) && matrix.get(slot).getAmount() >= item.getAmount()) {
                        int newAmount = matrix.get(slot).getAmount() - item.getAmount();
                        if (newAmount <= 0) {
                            daCrafter.getInventory().setItem(slot, null);
                        } else {
                            Objects.requireNonNull(daCrafter.getInventory().getItem(slot)).setAmount(newAmount);
                        }
                    } else {
                        return;
                    }
                }
            }
        }
        this.addResult(daCrafter, this.getResult());
    }

    public void startProcess(@NotNull DACrafter daCrafter) {
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
    public void finishProcess(@NotNull DACrafter daCrafter, boolean isAsync) {
        this.updateView(daCrafter, 0, isAsync);
        daCrafter.getProcess().reset();
    }

    /**
     * Cancels the process of the recipe
     *
     * @param daCrafter The table to cancel the process on
     * @param isAsync   If the method is called async
     */
    public void cancelProcess(@NotNull DACrafter daCrafter, boolean isAsync) {
        if (daCrafter.getProcess().isProcessing()) {
            Bukkit.getScheduler().cancelTask(daCrafter.getProcess().getTaskID());
            this.updateView(daCrafter, 0, isAsync);
            daCrafter.getProcess().reset();
        }

    }

    /**
     * Adds the result to the table
     * <br>
     * If the result slot is empty,
     * the result will be set in the result slot.
     * <br>
     * If the result is the same as the item in the result slot,
     * it will be added to the amount of the item in the result slot.
     * <br>
     * If the result is not the same as the item in the result slot, the result will be dropped
     *
     * @param daCrafter The table to add the result to
     * @param result    The result to add
     */
    private void addResult(@NotNull DACrafter daCrafter, DAItem result) {
        ItemStack itemStack = daCrafter.getInventory().getItem(daCrafter.getResultSlot());
        ItemStack resultItem = result != null ? result.getItemStack() : null;
        if (resultItem != null) {
            resultItem.setAmount(result.getAmount());
        }
        if (itemStack == null) {
            daCrafter.getInventory().setItem(daCrafter.getResultSlot(), resultItem);
        } else if (resultItem != null) {
            if (DAUtil.matchItems(itemStack, resultItem, result.getItemMatchTypes())) {
                if (itemStack.getAmount() + resultItem.getAmount() <= itemStack.getMaxStackSize()) {
                    itemStack.setAmount(itemStack.getAmount() + resultItem.getAmount());
                } else {
                    daCrafter.getWorld().dropItem(daCrafter.getBody().getSign().getLocation(), resultItem);
                }
            } else {
                daCrafter.getWorld().dropItem(daCrafter.getBody().getSign().getLocation().add(0, 1, 0), resultItem);
            }
        }
    }

    /**
     * Updates the view of the table
     *
     * @param daCrafter The table to update
     * @param state     The state to update to
     */
    public void updateView(@NotNull DACrafter daCrafter, int state, boolean isAsync) {
        daCrafter.updateView(state, isAsync);
    }

    @Override
    public String toString() {
        return super.toString().replace("DARecipe", "DACrafterRecipe").replace("}", "") + ", shape=" + shape + ", shapeKeys=" + shapeKeys + ", isShapeless=" + isShapeless + "}";
    }

    /**
     * Processes the materials of the recipe asynchronously and updates the view of the table
     */
    public static class ProcessMaterials implements Runnable {

        /**
         * The state of the process
         */
        private final int state;
        /**
         * The crafter to process the materials on
         */
        private final DACrafter daCrafter;
        /**
         * The recipe to process
         */
        private final DACrafterRecipe recipe;
        /**
         * The processing time of the recipe for each state
         */
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

    @Override
    public Component asComponent() {
        Component component = super.asComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommand(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.CRAFTER.getArg(), this.getID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    @Override
    //TODO: Make Translatable
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Process Time: " + this.getProcessingTime() + "s\n"));
        hover = hover.append(Component.text("Required Players: " + this.getRequiredPlayers() + "\n"));
        hover = hover.append(Component.text("Shape:"));
        for (String row : this.shape) {
            hover = hover.appendNewline().append(Component.text(row));
        }
        hover = super.getMaterials(hover, this.shapeKeys);
        return hover;
    }
}
