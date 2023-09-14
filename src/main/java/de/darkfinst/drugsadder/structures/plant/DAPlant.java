package de.darkfinst.drugsadder.structures.plant;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.DamageToolException;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAProbabilityItem;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public class DAPlant extends DAStructure {

    private final DAItem seed;
    private final boolean isCrop;
    private final float growTime;
    private final boolean destroyOnHarvest;
    private final DAItem[] drops;
    @Setter
    private Map<String, Integer> allowedTools = new HashMap<>();

    private long lastHarvest = 0;
    private boolean canBeHarvested = false;

    private final SecureRandom secureRandom;

    public DAPlant(DAItem seed, boolean isCrop, boolean destroyOnHarvest, float growTime, DAItem... drops) {
        this.seed = seed;
        this.isCrop = isCrop;
        this.destroyOnHarvest = destroyOnHarvest;
        this.growTime = growTime;
        this.drops = drops;

        this.secureRandom = new SecureRandom();
    }

    /**
     * Creates the plant
     * <p>
     * Checks if the player has the permission to create the plant
     *
     * @param plantBlock Block of the plant
     * @param player     Player, who wants to create the plant
     */
    public void create(Block plantBlock, Player player) throws RegisterStructureException {
        if (player.hasPermission("drugsadder.plant.create")) {
            DAPlantBody daPlantBody = new DAPlantBody(this, plantBlock);
            try {
                boolean isValid = daPlantBody.isValidPlant();
                if (isValid) {
                    super.setBody(daPlantBody);
                    boolean success = DA.loader.registerDAStructure(this, false);
                    if (success) {
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                        this.canBeHarvested = false;
                        this.lastHarvest = System.currentTimeMillis();
                        if (this.isCrop && daPlantBody.getPlantBLock().getBlockData() instanceof Ageable ageable) {
                            float tsp = (growTime / ageable.getMaximumAge());
                            Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new GrowRunnable(this, plantBlock, tsp), ((long) tsp * 20));
                        }
                    }
                }
            } catch (ValidateStructureException ignored) {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_NotValid"), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Plant_NoCreate"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    /**
     * Creates the plant
     * <p>
     * Checks if the plant is valid
     *
     * @param plantBlock Block of the plant
     * @param isAsync    If the plant should be created, async
     * @return True if the plant was successfully created and registered
     */
    public boolean create(Block plantBlock, boolean isAsync) throws RegisterStructureException {
        DAPlantBody daPlantBody = new DAPlantBody(this, plantBlock);
        boolean isValid = daPlantBody.isValidPlant();
        if (isValid) {
            super.setBody(daPlantBody);
            boolean success = DA.loader.registerDAStructure(this, isAsync);
            if (success) {
                this.canBeHarvested = false;
                this.lastHarvest = System.currentTimeMillis();
                if (this.isCrop && daPlantBody.getPlantBLock().getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                    float tsp = (growTime / ageable.getMaximumAge());
                    Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new GrowRunnable(this, plantBlock, tsp), ((long) tsp * 20));
                }
            }
            return success;
        }
        return false;
    }

    /**
     * Checks if the player has the permission to harvest the plant.
     * Also check if the plant is ready to harvest.
     *
     * @param player Player, who wants to harvest the plant
     */
    public void checkHarvest(Player player) {
        if (player.hasPermission("drugsadder.plant.harvest")) {
            String namespacedID = DAUtil.getNamespacedIDByItemStack(player.getInventory().getItemInMainHand());
            if (this.hasTool(player)) {
                try {
                    int damage = this.allowedTools.getOrDefault(namespacedID, 0);
                    ItemStack itemStack = DAUtil.damageTool(player.getInventory().getItemInMainHand(), damage);
                    player.getInventory().setItemInMainHand(itemStack);
                    if (this.canBeHarvested) {
                        this.executeHarvest();
                    } else {
                        long time = System.currentTimeMillis() - this.lastHarvest;
                        long passedTime = TimeUnit.MILLISECONDS.toSeconds(time);
                        if (passedTime > this.growTime && !this.isCrop) {
                            this.executeHarvest();
                            DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_Harvested"), DrugsAdderSendMessageEvent.Type.PLAYER);
                        } else if (this.isCrop && this.getBody().getPlantBLock().getBlockData() instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge()) {
                            this.executeHarvest();
                            DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_Harvested"), DrugsAdderSendMessageEvent.Type.PLAYER);
                        } else {
                            DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_NoReady"), DrugsAdderSendMessageEvent.Type.PLAYER);
                        }
                    }
                } catch (DamageToolException e) {
                    DA.loader.msg(player, DA.loader.languageReader.get("Player_Tool_NotEnoughDurability"), DrugsAdderSendMessageEvent.Type.PLAYER);
                }
            } else {
                DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_WrongTool", namespacedID), DrugsAdderSendMessageEvent.Type.PLAYER);
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Plant_NoHarvest"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

    public boolean hasTool(Player player) {
        String namespacedID = DAUtil.getNamespacedIDByItemStack(player.getInventory().getItemInMainHand());
        return this.allowedTools.containsKey(namespacedID);
    }

    /**
     * Executes the harvest of the plant.
     * <p>
     * Drops the items and destroys the plant if destroyOnHarvest is true
     */
    private void executeHarvest() {
        Location location = this.getBody().blocks.get(0).getLocation();
        for (DAItem drop : this.drops) {
            if (drop instanceof DAProbabilityItem probabilityItem && probabilityItem.getProbability() < 100) {
                float random = this.secureRandom.nextFloat();
                if ((random * 100) > probabilityItem.getProbability()) {
                    continue;
                }
            }
            ItemStack itemStack = drop.getItemStack();
            itemStack.setAmount(drop.getAmount());
            location.getWorld().dropItemNaturally(location, itemStack);
        }
        if (this.destroyOnHarvest) {
            DA.loader.unregisterDAStructure(this);
            for (Block block : this.getBody().blocks) {
                block.setType(Material.AIR);
            }
        } else {
            this.canBeHarvested = false;
            this.lastHarvest = System.currentTimeMillis();
            if (this.getBody().getPlantBLock().getBlockData() instanceof Ageable ageable) {
                ageable.setAge(0);
                this.getBody().getPlantBLock().setBlockData(ageable);
                float tsp = (growTime / ageable.getMaximumAge());
                Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, new GrowRunnable(this, this.getBody().getPlantBLock(), tsp), ((long) tsp * 20));
            }
        }
    }

    /**
     * Destroys the plant
     *
     * @param player Player, who destroyed the plant
     * @param block  Block of the plant
     */
    public void destroy(Player player, Block block) {
        if (DA.loader.unregisterDAStructure(player, block)) {
            List<Item> items = new ArrayList<>();
            items.add(this.getBody().getPlantBLock().getWorld().dropItemNaturally(this.getBody().blocks.get(0).getLocation(), this.seed.getItemStack()));
            BlockDropItemEvent blockDropItemEvent = new BlockDropItemEvent(this.getBody().blocks.get(0), this.getBody().blocks.get(0).getState(), player, items);
            Bukkit.getPluginManager().callEvent(blockDropItemEvent);
        }
    }

    /**
     * Destroys the plant
     */
    public void destroy() {
        if (DA.loader.unregisterDAStructure(this)) {
            this.getBody().getPlantBLock().getWorld().dropItemNaturally(this.getBody().blocks.get(0).getLocation(), this.seed.getItemStack());
        }
    }

    public static void handelChange(Block block) {
        if (Material.FARMLAND.equals(block.getType())) {
            Block crop = block.getRelative(0, 1, 0);
            if (DA.loader.isPlant(crop)) {
                DAStructure plant = DA.loader.getStructure(crop);
                if (plant instanceof DAPlant daPlant) {
                    daPlant.destroy();
                }
            }
        }
    }


    @Override
    public DAPlantBody getBody() {
        return (DAPlantBody) super.getBody();
    }


    /**
     * Runnable for growing the plant if it is a crop
     */
    public static class GrowRunnable implements Runnable {

        private final DAPlant plant;
        private final Block crop;
        private final float growTime;

        public GrowRunnable(DAPlant plant, Block crop, float growTime) {
            this.plant = plant;
            this.crop = crop;
            this.growTime = growTime;
        }

        @Override
        public void run() {
            if (crop.getBlockData() instanceof Ageable ageable) {
                int age = ageable.getAge();
                if (age < ageable.getMaximumAge()) {
                    int newAge = age + 1;
                    ageable.setAge(newAge);
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> crop.setBlockData(ageable));
                    if (newAge < ageable.getMaximumAge()) {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, this, Math.max(Math.round((long) (growTime * 20)), 1));
                    } else {
                        plant.canBeHarvested = true;
                    }
                }
            }
        }
    }

}
