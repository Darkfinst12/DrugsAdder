package de.darkfinst.drugsadder.structures.crafter;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.api.events.crafter.CrafterStartRecipeEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DACrafterRecipe;
import de.darkfinst.drugsadder.structures.DAInvStructure;
import lombok.Getter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DACrafter extends DAInvStructure {

    /**
     * The slots which are blocked
     */
    private final int[] blockedSlots = new int[]{5, 6, 7, 8, 14, 15, 16, 17, 24, 26, 32, 33, 34, 35, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
    /**
     * The slot of the result
     */
    private final int resultSlot = 25;
    /**
     * The slots of the materials
     */
    private final int[] materialSlots = new int[]{0, 1, 2, 3, 4, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22, 27, 28, 29, 30, 31, 36, 37, 38, 39, 40};
    /**
     * The slot wich starts the recipe
     */
    private final int startSlot = 23;

    public DACrafter() {
        super("Structure_Name_Crafter", 54, DAConfig.crafterStates, DAConfig.crafterTitleArray);
    }

    /**
     * Creates a crafter
     * <p>
     * It checks if the player has the permission to create a crafter and if the crafter is valid
     *
     * @param sign   The sign of the crafter
     * @param player The player who created the crafter
     */
    public void create(@NotNull Block sign, @NotNull Player player) throws RegisterStructureException {
        if (player.hasPermission("drugsadder.crafter.create")) {
            DACrafterBody dacrafterBody = new DACrafterBody(sign);
            try {
                boolean isValid = dacrafterBody.isValidCrafter();
                if (isValid) {
                    super.setBody(dacrafterBody);
                    boolean success = DA.loader.registerDAStructure(this, false);
                    if (success) {
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Crafter_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                    }
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Crafter_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Crafter_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    /**
     * Creates a crafter
     * <p>
     * It checks if the crafter is valid
     *
     * @param sign    The sign of the crafter
     * @param isAsync If the crafter should be created, async
     * @return True if the crafter was successfully created and registered
     */
    public boolean create(@NotNull Block sign, boolean isAsync) throws ValidateStructureException, RegisterStructureException {
        DACrafterBody crafterBody = new DACrafterBody(sign);
        boolean isValid = crafterBody.isValidCrafter();
        if (isValid) {
            super.setBody(crafterBody);
            DA.loader.registerDAStructure(this, isAsync);
        }
        return isValid;
    }

    /**
     * Opens the crafter inventory for the player
     * <p>
     * It checks if the player has the permission to open the crafter
     *
     * @param player The player who wants to open the crafter
     */
    public void open(@NotNull Player player) {
        if (player.hasPermission("drugsadder.crafter.open")) {
            player.openInventory(this.inventory);
            String title = LegacyComponentSerializer.legacyAmpersand().serialize(this.getTitle(this.getProcess().getState()));
            player.getOpenInventory().setTitle(ChatColor.translateAlternateColorCodes('&', title));
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perms_Crafter_NoOpen"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }


    /**
     * @return The body of the crafter
     */
    public DACrafterBody getBody() {
        return (DACrafterBody) super.getBody();
    }

    /**
     * @return The process of the crafter
     */
    public DACrafterProcess getProcess() {
        return this.getBody().getProcess();
    }


    /**
     * Returns a map that contains the contents of the crafter with the slot as key
     *
     * @return The content of the crafter
     */
    public Map<Integer, ItemStack> getContentMap() {
        Map<Integer, ItemStack> content = new HashMap<>();
        for (int materialSlot : this.materialSlots) {
            content.put(materialSlot, this.inventory.getItem(materialSlot));
        }
        return content;
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
                if (event.getSlot() == this.resultSlot && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                } else if ((Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) || this.startSlot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                }
            }
            default -> {
                if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == event.getSlot()) && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                } else if (this.startSlot == event.getSlot() && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(true);
                    this.callRecipeCheck(event.getWhoClicked());
                } else if (ClickType.SHIFT_LEFT.equals(event.getClick()) || ClickType.SHIFT_RIGHT.equals(event.getClick()) && event.getClickedInventory() != this.inventory) {
                    event.setCancelled(true);
                } else if (event.getSlot() == this.resultSlot && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(false);
                }
            }
        }
    }

    /**
     * Calls the recipe check, and if a recipe matches, it starts the process
     *
     * @param who The player who started the recipe, can be null
     */
    private void callRecipeCheck(@Nullable HumanEntity who) {
        List<DACrafterRecipe> recipes = DAConfig.daRecipeReader.getCrafterRecipes();
        for (DACrafterRecipe recipe : recipes) {
            boolean matchMaterials = recipe.matchMaterials(this.getContentMap());
            int viewers = this.inventory.getViewers().size();
            if (matchMaterials && recipe.getRequiredPlayers() <= viewers) {
                if (who != null) {
                    ((Player) who).playSound(who.getLocation(), Sound.UI_BUTTON_CLICK, 80, 1);
                }
                this.startRecipe(who, recipe);
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
    public void startRecipe(@Nullable HumanEntity who, @NotNull DACrafterRecipe recipe) {
        CrafterStartRecipeEvent crafterStartRecipeEvent = new CrafterStartRecipeEvent(who, this, recipe);
        Bukkit.getPluginManager().callEvent(crafterStartRecipeEvent);
        if (!crafterStartRecipeEvent.isCancelled()) {
            recipe.startProcess(this);
        }
    }

    /**
     * Handles the inventory drag event
     *
     * @param event The event to handle
     */
    public void handleInventoryDrag(InventoryDragEvent event) {
        for (Integer rawSlot : event.getRawSlots()) {
            if (Arrays.stream(this.blockedSlots).anyMatch(slot -> slot == rawSlot) || this.startSlot == rawSlot || this.resultSlot == rawSlot) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void handelInventoryClose(InventoryCloseEvent event) {
        int viewers = event.getInventory().getViewers().size() - 1;
        DACrafterRecipe recipe = this.getProcess().getRecipe();
        if (recipe != null && viewers < recipe.getRequiredPlayers()) {
            recipe.cancelProcess(this, false);
        }
        if (viewers == 0 && !DAConfig.crafterKeepInv) {
            this.dropContents();
        }
    }
}
