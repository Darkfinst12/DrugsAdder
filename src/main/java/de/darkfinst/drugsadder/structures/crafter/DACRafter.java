package de.darkfinst.drugsadder.structures.crafter;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DACRafter extends DAStructure implements InventoryHolder {

    /**
     * The inventory of the crafter
     */
    @Setter(AccessLevel.NONE)
    protected Inventory inventory;

    /**
     * The slots which are blocked
     */
    private final int[] blockedSlots = new int[]{5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 26, 32, 33, 34, 35, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53};
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

    public DACRafter() {
        this.inventory = DA.getInstance.getServer().createInventory(this, 54, this.getTitle(0));
    }

    /**
     * Gets the title of the crafter with the given state
     *
     * @param state The state of the crafter
     * @return The title of the crafter
     */
    public String getTitle(int state) {
        String title = DA.loader.languageReader.get("Structure_Name_Crafter");
        int[] titleArray = DAConfig.crafterTitleArray;
        int titleLength = title.length();
        if (title.contains("&")) {
            titleLength = titleLength - (int) (title.chars().filter(ch -> ch == '&').count() * 2);
        }

        return ChatColor.WHITE + DAUtil.convertWidthToMinecraftCode((titleLength * titleArray[0]) - titleArray[1]) + DAConfig.crafterStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(titleLength * titleArray[2]) + titleArray[3]) + ChatColor.translateAlternateColorCodes('&', title);
    }

    /**
     * Gets the title of the crafter with the given state
     * <p>
     * Note this is a debug method
     *
     * @param m1    The first multiplier
     * @param m2    The first subtractor
     * @param m3    The second multiplier
     * @param m4    The second adder
     * @param state The state of the crafter
     * @return The title of the crafter
     */
    public String getTitle(int m1, int m2, int m3, int m4, int state) {
        String title = DA.loader.languageReader.get("Structure_Name_Crafter");
        int titleLength = title.length();
        if (title.contains("&")) {
            titleLength = titleLength - (int) (title.chars().filter(ch -> ch == '&').count() * 2);
        }

        return ChatColor.WHITE + DAUtil.convertWidthToMinecraftCode((titleLength * m1) - m2) + DAConfig.crafterStates.get(state) + DAUtil.convertWidthToMinecraftCode(-(titleLength * m3) + m4) + ChatColor.translateAlternateColorCodes('&', title);
    }

    /**
     * Creates a crafter
     * <p>
     * It checks if the player has the permission to create a crafter and if the crafter is valid
     *
     * @param sign   The sign of the crafter
     * @param player The player who created the crafter
     */
    public void create(Block sign, Player player) throws RegisterStructureException {
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
    public boolean create(Block sign, boolean isAsync) throws ValidateStructureException, RegisterStructureException {
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
    public void open(Player player) {
        if (player.hasPermission("drugsadder.crafter.open")) {
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_HIT, 100, 0);
            player.openInventory(this.inventory);
            player.getOpenInventory().setTitle(this.getTitle(this.getProcess().getState()));
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

    public DACrafterProcess getProcess() {
        return this.getBody().getProcess();
    }

    /**
     * @return The inventory of the crafter
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public List<ItemStack> getContent() {
        List<ItemStack> content = new ArrayList<>();
        for (int materialSlot : this.materialSlots) {
            content.add(this.inventory.getItem(materialSlot));
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
                    //TODO: Start recipe
                } else if (ClickType.SHIFT_LEFT.equals(event.getClick()) || ClickType.SHIFT_RIGHT.equals(event.getClick()) && event.getClickedInventory() != this.inventory) {
                    event.setCancelled(true);
                } else if (event.getSlot() == this.resultSlot && event.getClickedInventory() == this.inventory) {
                    event.setCancelled(false);
                }
            }
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
        if (event.getInventory().getViewers().isEmpty()) {
            //TODO: Cancel Recipe
            if(!DAConfig.crafterKeepInv){
                this.destroyInventory();
            }
        }
    }

    /**
     * Drops the inventory of the crafter
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
