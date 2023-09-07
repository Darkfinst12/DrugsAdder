package de.darkfinst.drugsadder.structures.plant;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.exceptions.ValidateStructureException;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAProbabilityItem;
import de.darkfinst.drugsadder.structures.DAStructure;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class DAPlant extends DAStructure {

    private final DAItem seed;
    private final boolean isCrop;
    private final float growTime;
    private final boolean destroyOnHarvest;
    private final DAItem[] drops;

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

    public void create(Block plantBlock, Player player) {
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
                        if (this.isCrop && getBody().blocks.get(0).getBlockData() instanceof Ageable ageable) {
                            Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, new GrowRunnable(this, plantBlock, (growTime / ageable.getMaximumAge())));
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

    public void create(Block plantBlock) {
        try {
            DAPlantBody daPlantBody = new DAPlantBody(this, plantBlock);
            boolean isValid = daPlantBody.isValidPlant();
            if (isValid) {
                super.setBody(daPlantBody);
                boolean success = DA.loader.registerDAStructure(this, false);
                if (success) {
                    this.lastHarvest = System.currentTimeMillis();
                    if (this.isCrop && this.getBody().blocks.get(0).getBlockData() instanceof Ageable ageable) {
                        Bukkit.getScheduler().runTaskAsynchronously(DA.getInstance, new GrowRunnable(this, plantBlock, (growTime / ageable.getMaximumAge())));
                    }
                }
            }
        } catch (ValidateStructureException ignored) {
        }
    }

    public void checkHarvest(Player player) {
        if (player.hasPermission("drugsadder.plant.harvest")) {
            if (this.canBeHarvested) {
                this.executeHarvest();
            } else {
                long time = System.currentTimeMillis() - this.lastHarvest;
                long passedTime = TimeUnit.MILLISECONDS.toSeconds(time);
                if (passedTime > this.growTime) {
                    this.executeHarvest();
                }
            }
        } else {
            DA.loader.msg(player, DA.loader.languageReader.get("Perm_Plant_NoHarvest"), DrugsAdderSendMessageEvent.Type.PERMISSION);
        }
    }

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
        this.canBeHarvested = false;
        this.lastHarvest = System.currentTimeMillis();
    }

    public void destroy(Player player) {
        List<Item> items = new ArrayList<>();
        for (DAItem drop : this.drops) {
            items.add(this.getBody().blocks.get(0).getWorld().dropItemNaturally(this.getBody().blocks.get(0).getLocation(), drop.getItemStack()));
        }
        BlockDropItemEvent blockDropItemEvent = new BlockDropItemEvent(this.getBody().blocks.get(0), this.getBody().blocks.get(0).getState(), player, items);
        Bukkit.getPluginManager().callEvent(blockDropItemEvent);
    }


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
                    ageable.setAge(age + 1);
                    Bukkit.getScheduler().runTask(DA.getInstance, () -> crop.setBlockData(ageable));
                    Bukkit.getScheduler().runTaskLaterAsynchronously(DA.getInstance, this, Math.max(Math.round((long) (growTime * 20)), 1));
                } else {
                    plant.canBeHarvested = true;
                }
            } else {
                DA.log.errorLog("Crop is not ageable! - " + crop.getLocation());
            }
        }
    }

}
