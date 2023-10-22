package de.darkfinst.drugsadder.structures.plant;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.DamageToolException;
import de.darkfinst.drugsadder.exceptions.Structures.RegisterStructureException;
import de.darkfinst.drugsadder.exceptions.Structures.ValidateStructureException;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAProbabilityItem;
import de.darkfinst.drugsadder.structures.DAStructure;
import de.darkfinst.drugsadder.timed.TimedExecutable;
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
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public class DAPlant extends DAStructure {

    /**
     * The seed of the plant
     */
    private final DAItem seed;
    /**
     * If the plant is a crop
     */
    private final boolean isCrop;
    /**
     * The time the plant needs to grow in seconds
     */
    private final float growthTime;
    /**
     * Whether the plant should be destroyed on harvest
     */
    private final boolean destroyOnHarvest;
    /**
     * The drops of the plant
     */
    private final DAItem[] drops;
    /**
     * The secure random for the calculation of the probability items
     */
    private final SecureRandom secureRandom;
    /**
     * The allowed tools to harvest the plant
     */
    @Setter
    private Map<String, Integer> allowedTools = new HashMap<>();
    /**
     * The last harvest time of the plant
     */
    private long lastHarvest = 0;

    public DAPlant(DAItem seed, boolean isCrop, boolean destroyOnHarvest, float growthTime, DAItem... drops) {
        this.seed = seed;
        this.isCrop = isCrop;
        this.destroyOnHarvest = destroyOnHarvest;
        this.growthTime = growthTime;
        this.drops = drops;

        this.secureRandom = new SecureRandom();
    }

    /**
     * Handles the change of a Block if it is a farmland
     *
     * @param block The block that changed
     */
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

    /**
     * Creates the plant
     * <p>
     * Checks if the player has the permission to create the plant
     *
     * @param plantBlock Block of the plant
     * @param player     Player, who wants to create the plant
     */
    public void create(@NotNull Block plantBlock, @NotNull Player player) throws RegisterStructureException {
        if (player.hasPermission("drugsadder.plant.create")) {
            DAPlantBody daPlantBody = new DAPlantBody(this, plantBlock);
            try {
                boolean isValid = daPlantBody.isValidPlant();
                if (isValid) {
                    super.setBody(daPlantBody);
                    boolean success = DA.loader.registerDAStructure(this, false);
                    if (success) {
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_Created"), DrugsAdderSendMessageEvent.Type.PLAYER);
                        this.lastHarvest = System.currentTimeMillis();
                        if (this.isCrop && daPlantBody.getPlantBLock().getBlockData() instanceof Ageable ageable) {
                            long tsp = Math.round(this.growthTime / ageable.getMaximumAge());
                            DA.loader.getTimedExecutionManager().addExecutable(new TimedGrow(daPlantBody.getPlantBLock().getLocation(), tsp));
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
    public boolean create(@NotNull Block plantBlock, boolean isAsync) throws RegisterStructureException {
        DAPlantBody daPlantBody = new DAPlantBody(this, plantBlock);
        boolean isValid = daPlantBody.isValidPlant();
        if (isValid) {
            super.setBody(daPlantBody);
            boolean success = DA.loader.registerDAStructure(this, isAsync);
            if (success) {
                this.lastHarvest = System.currentTimeMillis();
                if (this.isCrop && daPlantBody.getPlantBLock().getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                    float tsp = (growthTime / ageable.getMaximumAge());
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
    public void checkHarvest(@NotNull Player player) {
        if (player.hasPermission("drugsadder.plant.harvest")) {
            String namespacedID = DAUtil.getNamespacedIDByItemStack(player.getInventory().getItemInMainHand());
            if (this.hasTool(player)) {
                try {
                    if (this.isCrop && this.getBody().getPlantBLock().getBlockData() instanceof Ageable ageable && ageable.getAge() == ageable.getMaximumAge()) {
                        this.executeHarvest(player, namespacedID);
                        DA.loader.msg(player, DA.loader.languageReader.get("Player_Plant_Harvested"), DrugsAdderSendMessageEvent.Type.PLAYER);

                    } else {
                        long time = System.currentTimeMillis() - this.lastHarvest;
                        long passedTime = TimeUnit.MILLISECONDS.toSeconds(time);
                        if (passedTime > this.growthTime && !this.isCrop) {
                            this.executeHarvest(player, namespacedID);
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

    public boolean hasTool(@NotNull Player player) {
        String namespacedID = DAUtil.getNamespacedIDByItemStack(player.getInventory().getItemInMainHand());
        return this.allowedTools.containsKey(namespacedID);
    }

    /**
     * Executes the harvest of the plant.
     * <p>
     * Drops the items and destroys the plant if destroyOnHarvest is true
     */
    private void executeHarvest(Player player, String namespacedID) {
        int damage = this.allowedTools.getOrDefault(namespacedID, 0);
        ItemStack tool = DAUtil.damageTool(player.getInventory().getItemInMainHand(), damage);
        player.getInventory().setItemInMainHand(tool);
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
            this.lastHarvest = System.currentTimeMillis();
            if (this.getBody().getPlantBLock().getBlockData() instanceof Ageable ageable) {
                ageable.setAge(0);
                this.getBody().getPlantBLock().setBlockData(ageable);
                float tsp = (growthTime / ageable.getMaximumAge());
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

    /**
     * @return The body of the plant
     */
    @Override
    public DAPlantBody getBody() {
        return (DAPlantBody) super.getBody();
    }

    public void setLastHarvest(long lastHarvest) {
        this.lastHarvest = lastHarvest;
    }

    @Override
    public void destroyInventory() {
        //Do nothing because plants don't have an inventory
    }


    /**
     * Runnable for growing the plant if it is a crop
     */
    public static class GrowRunnable implements Runnable {

        /**
         * The plant
         */
        private final DAPlant plant;
        /**
         * The crop
         */
        private final Block crop;
        /**
         * The time the plant needs to grow in seconds
         */
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
                    }
                }
            }
        }
    }

    public static class TimedGrow extends TimedExecutable {
        /**
         * The crop
         */
        private final Location cropLoc;
        /**
         * The time the plant needs to grow in seconds
         */
        private final long growTime;

        public TimedGrow(Location cropLoc, long growTime) {
            super(System.currentTimeMillis() + growTime * 1000);
            this.cropLoc = cropLoc;
            this.growTime = growTime;
        }

        @Override
        public void run() {
            Block crop = this.cropLoc.getBlock();
            if (crop.getBlockData() instanceof Ageable ageable) {
                int age = ageable.getAge();
                if (age < ageable.getMaximumAge()) {
                    int newAge = age + 1;
                    ageable.setAge(newAge);
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> crop.setBlockData(ageable));
                    if (newAge < ageable.getMaximumAge()) {
                        DA.loader.getTimedExecutionManager().addExecutable(new TimedGrow(this.cropLoc, this.growTime));
                    }
                }
            }
        }
    }

}
