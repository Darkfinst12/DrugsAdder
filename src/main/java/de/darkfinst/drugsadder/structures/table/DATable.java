package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.table.TableCancelRecipeEvent;
import de.darkfinst.drugsadder.api.events.table.TableStartRecipeEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class DATable extends DAStructure implements InventoryHolder {

    @Setter(AccessLevel.NONE)
    protected Inventory inventory;

    private final int[] blockedSlots = new int[]{5, 8};
    private final int resultSlot = 2;
    private final int[] materialSlots = new int[]{3, 4};
    private final int[] filterSlots = new int[]{0, 1};
    private final int[] fuelSlots = new int[]{6, 7};

    public DATable() {
        this.inventory = DA.getInstance.getServer().createInventory(this, InventoryType.DISPENSER, DA.loader.getTranslation("Lab Table", "Structure_Name_Table"));
    }

    public void create(Block sign, Player player) {
        if (player.hasPermission("drugsadder.table.create")) {
            DATableBody daTableBody = new DATableBody(this, sign);
            try {
                boolean isValid = daTableBody.isValidTable();
                if (isValid) {
                    super.setBody(daTableBody);
                    DA.loader.registerDAStructure(this, false);
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Table_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Table_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public void create(Block sign, boolean isAsync) throws ValidateStructureException {
        DATableBody tableBody = new DATableBody(this, sign);
        boolean isValid = tableBody.isValidTable();
        if (isValid) {
            super.setBody(tableBody);
            DA.loader.registerDAStructure(this, isAsync);
        }
    }

    public void open(Player player) {
        if (player.hasPermission("drugsadder.table.open")) {
            player.openInventory(this.inventory);
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Table_NoOpen"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public DATableBody getBody() {
        return (DATableBody) super.getBody();
    }


    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    private void callRecipeCheck(@Nullable HumanEntity who) {
        List<DATableRecipe> recipes = DAConfig.daRecipeReader.getTableRecipes();
        for (DATableRecipe recipe : recipes) {
            DA.log.debugLog("Checking Recipe: " + recipe.getNamedID());
            if (this.isThisRecipe(recipe)) {
                this.startRecipe(who, recipe);
            }
        }
    }

    public void startRecipe(@Nullable HumanEntity who, @NotNull DATableRecipe recipe) {
        DAItem fuelTwo = this.inventory.getItem(this.fuelSlots[1]) != null ? new DAItem(Objects.requireNonNull(this.inventory.getItem(this.fuelSlots[1]))) : null;
        DAItem materialTwo = this.inventory.getItem(this.materialSlots[1]) != null ? new DAItem(Objects.requireNonNull(this.inventory.getItem(this.materialSlots[1]))) : null;
        boolean hasSecondProcess = fuelTwo != null && materialTwo != null;
        TableStartRecipeEvent tableStartRecipeEvent = new TableStartRecipeEvent(who, this, recipe);
        Bukkit.getPluginManager().callEvent(tableStartRecipeEvent);
        if (!tableStartRecipeEvent.isCancelled()) {
            recipe.startProcess(this, hasSecondProcess);
        }
    }

    public boolean isThisRecipe(@NotNull DATableRecipe recipe) {
        boolean isValid = false;
        if (recipe.getFilterOne() != null) {
            isValid = DAUtil.matchItems(recipe.getFilterOne().getItemStack(), this.inventory.getItem(this.filterSlots[0]), recipe.getFilterOne().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("FilterOne not valid");
                DA.log.debugLog("Required: " + recipe.getFilterOne().getItemStack());
                DA.log.debugLog("Actual: " + this.inventory.getItem(this.filterSlots[0]));
                return isValid;
            }
        }
        if (recipe.getFilterTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getFilterTwo().getItemStack(), this.inventory.getItem(this.filterSlots[1]), recipe.getFilterTwo().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("FilterTwo not valid");
                return isValid;
            }
        }
        if (recipe.getFuelOne() != null) {
            isValid = DAUtil.matchItems(recipe.getFuelOne().getItemStack(), this.inventory.getItem(this.fuelSlots[0]), recipe.getFuelOne().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("FuelOne not valid");
                return isValid;
            }
        }
        if (recipe.getFuelTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getFuelTwo().getItemStack(), this.inventory.getItem(this.fuelSlots[1]), recipe.getFuelTwo().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("FuelTwo not valid");
                return isValid;
            }
        }
        if (recipe.getMaterialOne() != null) {
            isValid = DAUtil.matchItems(recipe.getMaterialOne().getItemStack(), this.inventory.getItem(this.materialSlots[0]), recipe.getMaterialOne().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("MaterialOne not valid");
                return isValid;
            }
        }
        if (recipe.getMaterialTwo() != null) {
            isValid = DAUtil.matchItems(recipe.getMaterialTwo().getItemStack(), this.inventory.getItem(this.materialSlots[1]), recipe.getMaterialTwo().getItemMatchTypes());
            if (!isValid) {
                DA.log.debugLog("MaterialTwo not valid");
                DA.log.debugLog("Required: " + recipe.getMaterialTwo().getItemStack());
                DA.log.debugLog("Actual: " + this.inventory.getItem(this.materialSlots[1]));
                return isValid;
            }
        }
        return isValid;
    }


    public void handleInventoryClick(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PLACE_ONE, PLACE_SOME, PLACE_ALL, SWAP_WITH_CURSOR, HOTBAR_SWAP -> {
                if (event.getSlot() == this.resultSlot) {
                    event.setCancelled(true);
                    return;
                } else if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                    return;
                }
            }
            default -> {
                if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                    return;
                } else if (Arrays.stream(this.materialSlots).anyMatch(slot -> slot == event.getSlot())
                        || Arrays.stream(this.filterSlots).anyMatch(slot -> slot == event.getSlot())
                        || Arrays.stream(this.fuelSlots).anyMatch(slot -> slot == event.getSlot())) {
                    this.cancelRecipe(event.getWhoClicked());
                    return;
                }
            }
        }
        if (this.inventory.equals(event.getClickedInventory()) && !event.isCancelled()) {
            Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> this.callRecipeCheck(event.getWhoClicked()), 5);
        }
    }

    private void cancelRecipe(HumanEntity who) {
        List<DATableRecipe> recipes = DAConfig.daRecipeReader.getTableRecipes();
        for (DATableRecipe recipe : recipes) {
            if (recipe.getInProcess().containsKey(this)) {
                //TODO: only if not enough materials
                recipe.cancelProcess(this, "InvEvent", false);
                TableCancelRecipeEvent tableCancelRecipeEvent = new TableCancelRecipeEvent(who, this, recipe);
                Bukkit.getPluginManager().callEvent(tableCancelRecipeEvent);
            }
        }
    }

    public void handleInventoryDrag(InventoryDragEvent event) {
        for (Integer rawSlot : event.getRawSlots()) {
            if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == rawSlot) || this.resultSlot == rawSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Override
    public void destroyInventory() {
        for (ItemStack content : this.inventory.getContents()) {
            if (content != null && !content.getType().equals(Material.AIR)) {
                this.getBody().getWorld().dropItemNaturally(this.getBody().getSign().getLocation(), content);
            }
        }
    }

    @Override
    public boolean hasInventory() {
        return true;
    }
}
