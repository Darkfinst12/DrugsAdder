package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.table.TableCancelRecipeEvent;
import de.darkfinst.drugsadder.api.events.table.TableStartRecipeEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Getter
public class DATable extends DAStructure implements InventoryHolder {

    /**
     * The inventory of the table
     */
    @Setter(AccessLevel.NONE)
    protected Inventory inventory;

    /**
     * The slots which are blocked
     */
    private final int[] blockedSlots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 16, 20, 21, 23, 24, 26, 27, 29, 30, 32, 33, 35, 36, 37, 39, 40, 41, 43, 44, 45, 47, 48, 49, 50, 51, 53};
    /**
     * The slot of the result
     */
    private final int resultSlot = 31;
    private final int finishSlot = 22;
    /**
     * The slots of the materials
     */
    private final int[] materialSlots = new int[]{28, 34};
    /**
     * The slots of the filters
     */
    private final int[] filterSlots = new int[]{19, 25};
    /**
     * The slots of the fuels
     */
    private final int[] fuelSlots = new int[]{46, 52};
    /**
     * The slots wich starts the recipe for the corresponding side
     */
    private final int[] startSlots = new int[]{38, 42};

    public DATable() {
        this.inventory = DA.getInstance.getServer().createInventory(this, 54, this.getTitle(0));
    }

    /**
     * Gets the title of the table with the given state
     *
     * @param state The state of the table
     * @return The title of the table
     */
    public String getTitle(int state) {
        String title = DA.loader.languageReader.get("Structure_Name_Table");
        int[] titleArray = DAConfig.tableTitleArray;


        return ChatColor.WHITE + DAUtil.convertWidthToMinecraftCode((title.length() * titleArray[0]) - titleArray[1]) + DAConfig.tableStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(title.length() * titleArray[2]) + titleArray[3]) + title;
    }

    /**
     * Gets the title of the table with the given state
     * <p>
     * Note this is a debug method
     *
     * @param m1    The first multiplier
     * @param m2    The first subtractor
     * @param m3    The second multiplier
     * @param m4    The second adder
     * @param state The state of the table
     * @return The title of the table
     */
    public String getTitle(int m1, int m2, int m3, int m4, int state) {
        String title = DA.loader.languageReader.get("Structure_Name_Table");

        return ChatColor.WHITE + DAUtil.convertWidthToMinecraftCode((title.length() * m1) - m2) + DAConfig.tableStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(title.length() * m3) + m4) + title;
    }

    /**
     * Creates a table
     * <p>
     * It checks if the player has the permission to create a table and if the table is valid
     *
     * @param sign   The sign of the table
     * @param player The player who created the table
     */
    public void create(Block sign, Player player) throws RegisterStructureException {
        if (player.hasPermission("drugsadder.table.create")) {
            DATableBody daTableBody = new DATableBody(sign);
            try {
                boolean isValid = daTableBody.isValidTable();
                if (isValid) {
                    super.setBody(daTableBody);
                    boolean success = DA.loader.registerDAStructure(this, false);
                    if (success) {
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                    }
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Table_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    /**
     * Creates a table
     * <p>
     * It checks if the table is valid
     *
     * @param sign    The sign of the table
     * @param isAsync If the table should be created, async
     * @return True if the table was successfully created and registered
     */
    public boolean create(Block sign, boolean isAsync) throws ValidateStructureException, RegisterStructureException {
        DATableBody tableBody = new DATableBody(sign);
        boolean isValid = tableBody.isValidTable();
        if (isValid) {
            super.setBody(tableBody);
            DA.loader.registerDAStructure(this, isAsync);
        }
        return isValid;
    }

    /**
     * Opens the table inventory for the player
     * <p>
     * It checks if the player has the permission to open the table
     *
     * @param player The player who wants to open the table
     */
    public void open(Player player) {
        if (player.hasPermission("drugsadder.table.open")) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 100, 0);
            player.openInventory(this.inventory);
            player.getOpenInventory().setTitle(this.getTitle(this.getProcess().getState()));
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Table_NoOpen"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    /**
     * @return The body of the table
     */
    public DATableBody getBody() {
        return (DATableBody) super.getBody();
    }

    public DATableProcess getProcess() {
        return this.getBody().getProcess();
    }


    /**
     * @return The inventory of the table
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }


    /**
     * Checks if the table is in a process for the given recipe and starts the process if it is not
     * <p>
     * Starts the process only if the recipe is valid for more information see {@link DATable#isThisRecipe(DATableRecipe, int)}
     *
     * @param who The player who clicked the table
     */
    private void callRecipeCheck(@Nullable HumanEntity who, int side) {
        DATableProcess process = this.getProcess();
        if (process.getTaskID() != -1) {
            return;
        }
        if (side == 0 && process.getRecipeOne() != null) {
            return;
        }
        if (side == 1 && process.getRecipeTwo() != null) {
            return;
        }
        List<DATableRecipe> recipes = DAConfig.daRecipeReader.getTableRecipes();
        for (DATableRecipe recipe : recipes) {
            if (this.isThisRecipe(recipe, side)) {
                if (who != null) {
                    ((Player) who).playSound(who.getLocation(), Sound.UI_BUTTON_CLICK, 80, 1);
                }
                this.startRecipe(who, recipe, side);
                return;
            }
        }
    }

    /**
     * Starts the process of the given recipe
     *
     * @param who    The player who started the recipe, can be null
     * @param recipe The recipe to start
     */
    public void startRecipe(@Nullable HumanEntity who, @NotNull DATableRecipe recipe, int side) {
        TableStartRecipeEvent tableStartRecipeEvent = new TableStartRecipeEvent(who, this, recipe);
        Bukkit.getPluginManager().callEvent(tableStartRecipeEvent);
        if (!tableStartRecipeEvent.isCancelled()) {
            recipe.startProcess(this, side);
        }
    }

    /**
     * Checks if the table has all items for the given recipe
     *
     * @param recipe The recipe to check
     * @return True if the table has all items for the recipe otherwise false
     */
    public boolean isThisRecipe(@NotNull DATableRecipe recipe, int side) throws IllegalArgumentException {
        if (side > 1 || side < 0) {
            throw new IllegalArgumentException("Side must be 0 or 1");
        }
        return side == 0 ? this.isThisRecipeOne(recipe) : this.isThisRecipeTwo(recipe);
    }

    public boolean isThisRecipeOne(@NotNull DATableRecipe recipe) {
        boolean isValid = false;
        if (recipe.getFilterOne() != null) {
            isValid = DAUtil.matchItems(recipe.getFilterOne().getItemStack(), this.inventory.getItem(this.filterSlots[0]), recipe.getFilterOne().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        if (recipe.getFuelOne() != null) {
            isValid = DAUtil.matchItems(recipe.getFuelOne().getItemStack(), this.inventory.getItem(this.fuelSlots[0]), recipe.getFuelOne().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        if (recipe.getMaterialOne() != null) {
            isValid = DAUtil.matchItems(recipe.getMaterialOne().getItemStack(), this.inventory.getItem(this.materialSlots[0]), recipe.getMaterialOne().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        return isValid;
    }

    public boolean isThisRecipeTwo(@NotNull DATableRecipe recipe) {
        boolean isValid = false;
        if (recipe.getFilterTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getFilterTwo().getItemStack(), this.inventory.getItem(this.filterSlots[1]), recipe.getFilterTwo().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        if (recipe.getFuelTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getFuelTwo().getItemStack(), this.inventory.getItem(this.fuelSlots[1]), recipe.getFuelTwo().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        if (recipe.getMaterialTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getMaterialTwo().getItemStack(), this.inventory.getItem(this.materialSlots[1]), recipe.getMaterialTwo().getItemMatchTypes());
            if (!isValid) {
                return isValid;
            }
        }
        return isValid;
    }

    /**
     * Handles the inventory click event
     * <p>
     * Depending on the action, it cancels the event or calls the recipe check
     *
     * @param event The event to handle
     */
    public void handleInventoryClick(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PLACE_ONE, PLACE_SOME, PLACE_ALL, SWAP_WITH_CURSOR, HOTBAR_SWAP -> {
                if ((event.getSlot() == this.resultSlot || event.getSlot() == this.finishSlot) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                } else if ((Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) || Arrays.stream(this.startSlots).anyMatch(slot -> slot == event.getSlot())) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                }
            }
            default -> {
                if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                } else if (Arrays.stream(this.materialSlots).anyMatch(slot -> slot == event.getSlot())
                        || Arrays.stream(this.filterSlots).anyMatch(slot -> slot == event.getSlot())
                        || Arrays.stream(this.fuelSlots).anyMatch(slot -> slot == event.getSlot())) {
                    int side = getClickedSide(event.getSlot());
                    if (side == this.getProcess().getSide()) {
                        this.cancelRecipe(event.getWhoClicked());
                    }
                } else if (Arrays.stream(this.startSlots).anyMatch(slot -> slot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                    int side = this.startSlots[0] == event.getSlot() ? 0 : 1;
                    Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> this.callRecipeCheck(event.getWhoClicked(), side), 1);
                } else if (this.finishSlot == event.getSlot() && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                    if (this.getProcess().isFinished()) {
                        ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(), Sound.UI_BUTTON_CLICK, 80, 1);
                        Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> this.getProcess().finish(this, false), 1);
                    }
                } else if (ClickType.SHIFT_LEFT.equals(event.getClick()) || ClickType.SHIFT_RIGHT.equals(event.getClick())) {
                    event.setCancelled(true);
                } else if (event.getSlot() == this.resultSlot && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(false);
                }
            }
        }
    }

    private int getClickedSide(int slot) {
        if (materialSlots[0] == slot || filterSlots[0] == slot || fuelSlots[0] == slot) {
            return 0;
        } else if (materialSlots[1] == slot || filterSlots[1] == slot || fuelSlots[1] == slot) {
            return 1;
        } else {
            return -1;
        }

    }

    /**
     * Handles the inventory drag event
     *
     * @param event The event to handle
     */
    public void handleInventoryDrag(InventoryDragEvent event) {
        for (Integer rawSlot : event.getRawSlots()) {
            if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == rawSlot) || Arrays.stream(this.startSlots).anyMatch(slot -> slot == rawSlot) || this.resultSlot == rawSlot || this.finishSlot == rawSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Cancels the recipe of the table
     * <p>
     * Calls the {@link TableCancelRecipeEvent}
     *
     * @param who The player who canceled the recipe
     */
    private void cancelRecipe(HumanEntity who) {

    }

    /**
     * Drops the inventory of the table
     */
    @Override
    public void destroyInventory() {
        for (ItemStack content : this.inventory.getContents()) {
            if (content != null && !content.getType().equals(Material.AIR)) {
                this.getBody().getWorld().dropItemNaturally(this.getBody().getSign().getLocation(), content);
            }
        }
    }

    /**
     * It is an override of {@link DAStructure#hasInventory()}
     *
     * @return true
     */
    @Override
    public boolean hasInventory() {
        return true;
    }
}
