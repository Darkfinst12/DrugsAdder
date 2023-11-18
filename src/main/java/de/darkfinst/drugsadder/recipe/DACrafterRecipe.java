package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

@Getter
public class DACrafterRecipe extends DAShapedRecipe {

    /**
     * The processing time of the recipe
     * <br>
     * The Time is in seconds
     */
    private final double processTime;

    /**
     * The required players for the recipe
     */
    private final int requiredPlayers;

    public DACrafterRecipe(String recipeID, RecipeType recipeType, DAItem result, double processTime, int requiredPlayers, DAItem... materials) {
        super(recipeID, recipeType, result, materials);
        this.processTime = processTime;
        this.requiredPlayers = requiredPlayers;
    }

    /**
     * Checks if the given items are materials of the recipe
     *
     * @param matrix The items to check
     * @return If the given items are materials of the recipe
     */
    public boolean matchMaterials(@NotNull Map<Integer, ItemStack> matrix) {
        try {
            if (super.isShapeless()) {
                return this.hasMaterials(matrix.values().toArray(new ItemStack[0]));
            } else {
                return this.matchShape(matrix);
            }
        } catch (IllegalArgumentException e) {
            DA.log.debugLog(e.getMessage());
            return false;
        }

    }

    /**
     * Matches the shape of the recipe with the given matrix
     *
     * @param matrix The matrix to match the shape with
     * @return If the shape matches
     * @throws IllegalArgumentException If the recipe is shapeless or the matrix length isn't 25
     */
    public boolean matchShape(@NotNull Map<Integer, ItemStack> matrix) throws IllegalArgumentException {
        if (super.isShapeless()) {
            throw new IllegalArgumentException("Recipe is shapeless");
        }
        if (matrix.size() != 25) {
            throw new IllegalArgumentException("Matrix length must be 25");
        }

        for (int i = 0; i < 5; i++) {
            String row = super.getShape().get(i);
            for (int j = 0; j < 5; j++) {
                String key = String.valueOf(row.charAt(j));
                if (!key.equals(" ")) {
                    DAItem item = super.getShapeKeys().get(key);
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
        if (super.isShapeless()) {
            throw new IllegalArgumentException("Recipe is shapeless");
        }
        if (matrix.size() != 25) {
            throw new IllegalArgumentException("Matrix length must be 25");
        }
        for (int i = 0; i < 5; i++) {
            String row = super.getShape().get(i);
            for (int j = 0; j < 5; j++) {
                String key = String.valueOf(row.charAt(j));
                if (!key.equals(" ")) {
                    DAItem item = super.getShapeKeys().get(key);
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

    /**
     * Starts the process of the recipe
     *
     * @param daCrafter The crafter to start the process on
     */
    public void startProcess(@NotNull DACrafter daCrafter) {
        daCrafter.getProcess().setRecipe(this);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, new ProcessMaterials(0, daCrafter, this, this.processTime / 7));
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
        return super.toString().replace("DARecipe", "DACrafterRecipe").replace("}", "") + ", shape=" + super.getShape() + ", shapeKeys=" + super.getShapeKeys() + ", isShapeless=" + super.isShapeless() + "}";
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

    /**
     * This method generates a component that represents the recipe.
     * <br>
     * It only shows the ID but extends a Hover Event that shows the process time and the materials.
     * <br>
     * It also extends a Click Event that executes the command to show the recipe in the info command.
     * <br>
     * For use see {@link de.darkfinst.drugsadder.commands.ListCommand}
     *
     * @return The component that represents the recipe.
     */
    @Override
    public @NotNull Component asListComponent() {
        Component component = super.asListComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommandString(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.CRAFTER.getArg(), this.getRecipeID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * Returns the hover event of the recipe
     *
     * @return The hover event of the recipe
     */
    @Override
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_ProcessTime", this.getProcessTime() + ""));
        hover = hover.appendNewline().append(DA.loader.languageReader.getComponentWithFallback("Miscellaneous_Components_RequiredPlayers", this.getRequiredPlayers() + ""));
        hover = getShapeComponent(hover);
        hover = super.getMaterialsAsComponent(hover);
        return hover;
    }
}
