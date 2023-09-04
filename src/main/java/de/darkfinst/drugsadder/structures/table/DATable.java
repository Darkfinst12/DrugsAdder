package de.darkfinst.drugsadder.structures.table;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DATableRecipe;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public class DATable extends DAStructure implements InventoryHolder {

    private final Inventory inventory;

    private final int[] blockedSlots = new int[]{5, 8};
    private final int resultSlot = 2;
    private final int[] materialSlots = new int[]{0, 1};
    private final int[] fillSlots = new int[]{3, 4};
    private final int[] fuelSlots = new int[]{6, 7};

    public DATable() {
        this.inventory = DA.getInstance.getServer().createInventory(this, InventoryType.DISPENSER, "Lab Table");
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

    private void callRecipe() {
        List<DATableRecipe> recipes = DAConfig.daRecipeReader.getTableRecipes();
        for (DATableRecipe recipe : recipes) {
            boolean isValid = false;
            if (recipe.getFilterOne() != null) {
                isValid = DAUtil.matchItems(recipe.getFilterOne().getItemStack(), this.inventory.getItem(this.fillSlots[0]), recipe.getFilterOne().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (recipe.getFilterTwo() != null) {
                isValid = DAUtil.matchItems(recipe.getFilterTwo().getItemStack(), this.inventory.getItem(this.fillSlots[0]), recipe.getFilterTwo().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (recipe.getFuelOne() != null) {
                isValid = DAUtil.matchItems(recipe.getFuelOne().getItemStack(), this.inventory.getItem(this.fuelSlots[0]), recipe.getFuelOne().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (recipe.getFuelTwo() != null) {
                isValid = DAUtil.matchItems(recipe.getFuelTwo().getItemStack(), this.inventory.getItem(this.fuelSlots[1]), recipe.getFuelTwo().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (recipe.getMaterialOne() != null) {
                isValid = DAUtil.matchItems(recipe.getMaterialOne().getItemStack(), this.inventory.getItem(this.materialSlots[0]), recipe.getMaterialOne().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (recipe.getMaterialTwo() != null) {
                isValid = DAUtil.matchItems(recipe.getMaterialTwo().getItemStack(), this.inventory.getItem(this.materialSlots[1]), recipe.getMaterialTwo().getItemMatchTypes());
                if (!isValid) {
                    continue;
                }
            }
            if (isValid) {
                DAItem fuelTwo = this.inventory.getItem(this.fuelSlots[1]) != null ? new DAItem(Objects.requireNonNull(this.inventory.getItem(this.fuelSlots[1]))) : null;
                DAItem materialTwo = this.inventory.getItem(this.materialSlots[1]) != null ? new DAItem(Objects.requireNonNull(this.inventory.getItem(this.materialSlots[1]))) : null;
                boolean hasSecondProcess = fuelTwo != null && materialTwo != null;
                recipe.startProcess(this, hasSecondProcess);
            }
        }
    }


    public void handleInventoryClick(InventoryClickEvent event) {
        switch (event.getAction()) {
            case PLACE_ONE, PLACE_SOME, PLACE_ALL -> {
                if (event.getSlot() == this.resultSlot) {
                    event.setCancelled(true);
                    return;
                }
            }
            default -> {
                if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
        if (this.inventory.equals(event.getClickedInventory())) {
            this.callRecipe();
        }
    }
}
