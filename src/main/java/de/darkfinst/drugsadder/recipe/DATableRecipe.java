package de.darkfinst.drugsadder.recipe;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.commands.DACommandManager;
import de.darkfinst.drugsadder.commands.InfoCommand;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@Getter
public class DATableRecipe extends DARecipe {

    /**
     * The filter for the first material
     */
    private final DAItem filterOne;
    /**
     * The fuel for the first material
     */
    private final DAItem fuelOne;
    /**
     * The first material
     */
    private final DAItem materialOne;
    /**
     * The duration of the first process in seconds
     */
    private final double processingTimeOne;
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
     * The fuel for the second material
     */
    private DAItem fuelTwo;
    /**
     * The second material
     */
    private DAItem materialTwo;
    /**
     * The duration of the second process in seconds
     */
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
            startProcessOne(daTable);
        } else if (side == 1) {
            startProcessTwo(daTable);
        }

    }

    /**
     * Restarts the process of the recipe
     *
     * @param daTable The table to restart the process on
     * @param state   The state to restart the process on
     */
    public void restartProcess(@NotNull DATable daTable, int state) {
        if (state < 5) {
            startProcessOne(daTable);
        } else if (state < 10) {
            startProcessTwo(daTable);
        } else if (state < 15) {
            startProcessOne(daTable);
        } else if (state < 20) {
            startProcessTwo(daTable);
        }
    }

    /**
     * Starts the first process of the recipe on the table
     *
     * @param daTable The table to start the process on
     */
    private void startProcessOne(@NotNull DATable daTable) {
        boolean otherFinished = daTable.getProcess().getRecipeTwo() != null;
        daTable.getProcess().setState(otherFinished ? 10 : 0);
        ProcessMaterialOne processMaterialOne = new ProcessMaterialOne(daTable, this, daTable.getProcess().getState(), processingTimeOne / 4, otherFinished);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialOne);
        daTable.getProcess().setRecipeOne(this);
        daTable.getProcess().setTaskID(task.getTaskId());
    }

    /**
     * Starts the second process of the recipe on the table if the first process is finished
     *
     * @param daTable The table to start the process on
     */
    private void startProcessTwo(@NotNull DATable daTable) {
        boolean otherFinished = daTable.getProcess().getRecipeOne() != null;
        daTable.getProcess().setState(otherFinished ? 5 : 0);
        ProcessMaterialTwo processMaterialTwo = new ProcessMaterialTwo(daTable, this, daTable.getProcess().getState(), processingTimeTwo / 4, otherFinished);
        BukkitTask task = Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, processMaterialTwo);
        daTable.getProcess().setRecipeTwo(this);
        daTable.getProcess().setTaskID(task.getTaskId());
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
    public void finishProcess(@NotNull DATable daTable, boolean isAsync) {
        this.updateView(daTable, 0, isAsync);
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
    public void cancelProcess(@NotNull DATable daTable, boolean isAsync) {
        if (daTable.getProcess().isProcessing()) {
            Bukkit.getScheduler().cancelTask(daTable.getProcess().getTaskID());
            this.updateView(daTable, 0, isAsync);
            daTable.getProcess().reset();
            DAItem result = DAUtil.getItemStackByNamespacedID(DAConfig.cancelRecipeItem);
            this.addResult(daTable, result);
        }

    }

    /**
     * Adds the result to the table
     * <br>
     * If the result slot is empty, the result will be added to the result slot
     * <br>
     * If the result slot is not empty, the result will be dropped at the location of the table
     *
     * @param daTable The table to add the result to
     * @param result  The result to add
     */
    private void addResult(@NotNull DATable daTable, DAItem result) {
        ItemStack resultItem = result != null ? result.getItemStack() : null;
        if (daTable.getInventory().getItem(daTable.getResultSlot()) == null) {
            daTable.getInventory().setItem(daTable.getResultSlot(), resultItem);
        } else if (resultItem != null) {
            daTable.getWorld().dropItem(daTable.getBody().getSign().getLocation(), resultItem);
        }
    }

    /**
     * Updates the view of the table
     *
     * @param daTable The table to update
     * @param state   The state to update to
     */
    public void updateView(DATable daTable, int state, boolean isAsync) {
        daTable.updateView(state, isAsync);
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

    /**
     * Runnable for the first process
     */
    public static class ProcessMaterialOne implements Runnable {

        /**
         * The state of the process
         */
        private final int state;
        /**
         * The table to process on
         */
        private final DATable daTable;
        /**
         * The recipe to process
         */
        private final DATableRecipe recipe;
        /**
         * If the second process is finished
         */
        private final int otherFinished;
        /**
         * The duration of the process in seconds
         */
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
                    DA.log.errorLog("Invalid State: " + state, true);
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

        /**
         * The state of the process
         */
        private final int state;
        /**
         * The table to process on
         */
        private final DATable daTable;
        /**
         * The recipe to process
         */
        private final DATableRecipe recipe;
        /**
         * The duration of the process in seconds
         */
        private final double processingTime;
        /**
         * If the first process is finished
         */
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
                    DA.log.errorLog("Invalid State: " + state, true);
                }
            } catch (Exception e) {
                DA.log.logException(e, true);
            }
        }
    }

    /**
     * This method generates a component that represents the recipe.
     *
     * @return The component that represents the recipe.
     */
    @Override
    public @NotNull Component asComponent() {
        Component component = super.asComponent();
        component = component.hoverEvent(this.getHover().asHoverEvent());
        String command = DACommandManager.buildCommand(DACommandManager.PossibleArgs.INFO.getArg(), InfoCommand.PossibleArgs.RECIPES.getArg(), InfoCommand.PossibleArgs.TABLE.getArg(), this.getID());
        return component.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    /**
     * Returns the hover event of the recipe
     *
     * @return The hover event of the recipe
     */
    //TODO: Make Translatable
    @Override
    public @NotNull Component getHover() {
        Component hover = Component.text().asComponent();
        hover = hover.append(Component.text("Processing Time One: " + this.getProcessingTimeOne() + "s\n"));
        hover = hover.append(Component.text("Filter One: "));
        Component name = this.getFilterOne().getName();
        if (name == null) {
            name = Component.text(this.getFilterOne().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getFilterOne().getAmount() + " ")).append(name).append(Component.text("\n"));
        hover = hover.append(Component.text("Fuel One: "));
        name = this.getFuelOne().getName();
        if (name == null) {
            name = Component.text(this.getFuelOne().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getFuelOne().getAmount() + " ")).append(name).append(Component.text("\n"));
        hover = hover.append(Component.text("Material One: "));
        name = this.getMaterialOne().getName();
        if (name == null) {
            name = Component.text(this.getMaterialOne().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getMaterialOne().getAmount() + " ")).append(name).append(Component.text("\n"));

        hover = hover.append(Component.text("Processing Time Two: " + this.getProcessingTimeTwo() + "s\n"));
        hover = hover.append(Component.text("Filter Two: "));
        name = this.getFilterTwo().getName();
        if (name == null) {
            name = Component.text(this.getFilterTwo().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getFilterTwo().getAmount() + " ")).append(name).append(Component.text("\n"));
        hover = hover.append(Component.text("Fuel Two: "));
        name = this.getFuelTwo().getName();
        if (name == null) {
            name = Component.text(this.getFuelTwo().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getFuelTwo().getAmount() + " ")).append(name).append(Component.text("\n"));
        hover = hover.append(Component.text("Material Two: "));
        name = this.getMaterialTwo().getName();
        if (name == null) {
            name = Component.text(this.getMaterialTwo().getItemStack().getType().name());
        }
        hover = hover.append(Component.text("x" + this.getMaterialTwo().getAmount() + " ")).append(name).append(Component.text("\n"));
        return hover;
    }


}
