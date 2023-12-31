package de.darkfinst.drugsadder.structures;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.crafter.DACrafter;
import de.darkfinst.drugsadder.structures.plant.DAPlant;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Setter
@Getter
public abstract class DAStructure {

    /**
     * The body of the structure
     */
    private DABody body;

    /**
     * If the structure should be removed on the next server start
     */
    private boolean forRemoval = false;

    /**
     * Saves the structure to the config file
     *
     * @param config  The config file to save the structure to
     * @param oldData The old data to merge into the new data
     */
    public static void save(ConfigurationSection config, ConfigurationSection oldData) {
        DAUtil.createWorldSections(config);

        if (!DA.loader.getStructureList().isEmpty()) {
            int barrelID = 0;
            int pressID = 0;
            int tableID = 0;
            int plantID = 0;
            int crafterID = 0;
            for (DAStructure structure : DA.loader.getStructureList()) {
                if (structure instanceof DABarrel barrel) {
                    String worldName = barrel.getWorld().getUID().toString();
                    String prefix = worldName + "." + "barrels." + barrelID;

                    Location loc = barrel.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

                    config.set(prefix + ".forRemoval", barrel.isForRemoval());

                    barrelID = saveInventory(config, barrelID, prefix, barrel.getInventory());
                } else if (structure instanceof DAPress press) {
                    String worldName = press.getWorld().getUID().toString();
                    String prefix = worldName + "." + "presses." + pressID;

                    Location loc = press.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

                    config.set(prefix + ".forRemoval", press.isForRemoval());

                    ConfigurationSection invConfig = config.createSection(prefix + ".inv");
                    int slot = 0;
                    for (ItemStack compressedItem : press.getCompressedItems()) {
                        invConfig.set(slot + "", compressedItem);
                        slot++;
                    }


                    pressID++;
                } else if (structure instanceof DATable table) {
                    String worldName = table.getWorld().getUID().toString();
                    String prefix = worldName + "." + "tables." + tableID;

                    Location loc = table.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

                    config.set(prefix + ".forRemoval", table.isForRemoval());

                    config.set(prefix + ".process.state", table.getProcess().getState());
                    config.set(prefix + ".process.recipe.one", table.getProcess().getRecipeOne() == null ? "null" : table.getProcess().getRecipeOne().getRecipeID());
                    config.set(prefix + ".process.recipe.two", table.getProcess().getRecipeTwo() == null ? "null" : table.getProcess().getRecipeTwo().getRecipeID());

                    tableID = saveInventory(config, tableID, prefix, table.getInventory());
                } else if (structure instanceof DACrafter crafter) {
                    String worldName = crafter.getWorld().getUID().toString();
                    String prefix = worldName + "." + "crafters." + crafterID;

                    Location loc = crafter.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

                    config.set(prefix + ".forRemoval", crafter.isForRemoval());

                    config.set(prefix + ".process.state", crafter.getProcess().getState());
                    config.set(prefix + ".process.recipe", crafter.getProcess().getRecipe() == null ? "null" : crafter.getProcess().getRecipe().getRecipeID());

                    crafterID = saveInventory(config, crafterID, prefix, crafter.getInventory());
                } else if (structure instanceof DAPlant plant) {
                    String worldName = plant.getWorld().getUID().toString();
                    String prefix = worldName + "." + "plants." + plantID;

                    config.set(prefix + ".forRemoval", plant.isForRemoval());
                    config.set(prefix + ".lastHarvest", plant.getLastHarvest());

                    Location loc = plant.getBody().getPlantBLock().getLocation();
                    config.set(prefix + ".plant", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
                    config.set(prefix + ".seed", plant.getSeed().getNamespacedID());

                    plantID++;
                }
            }
        }

        if (oldData != null) {
            DA.log.debugLog("Merging old data into new data");
            for (String world : oldData.getKeys(false)) {
                ConfigurationSection structureSection = oldData.getConfigurationSection(world);
                assert structureSection != null;
                for (String structure : structureSection.getKeys(false)) {
                    ConfigurationSection structureData = structureSection.getConfigurationSection(structure);
                    assert structureData != null;
                    for (String key : structureData.getKeys(false)) {
                        if (!config.contains(world + "." + structure + "." + key)) {
                            config.set(world + "." + structure + "." + key, oldData.get(world + "." + structure + "." + key));
                        }
                    }
                }
            }
        } else {
            DA.log.debugLog("No old data to merge");
        }
    }

    /**
     * Saves the inventory of the structure to the config file
     *
     * @param config    The config file to save the inventory to
     * @param id        The id of the structure
     * @param prefix    The prefix of the structure
     * @param inventory The inventory to save
     * @return The new id
     */
    private static int saveInventory(ConfigurationSection config, int id, String prefix, Inventory inventory) {
        int slot = 0;
        ItemStack item;
        ConfigurationSection invConfig = null;
        while (slot < inventory.getSize()) {
            item = inventory.getItem(slot);
            if (item != null) {
                if (invConfig == null) {
                    invConfig = config.createSection(prefix + ".inv");
                }
                // ItemStacks are configurationSerializeable, makes them
                // really easy to save
                invConfig.set(slot + "", item);
            }

            slot++;
        }


        id++;
        return id;
    }

    /**
     * Checks if the block is a body part of the structure
     *
     * @param block The block to check
     * @return true, if the block is a body part otherwise false
     */
    public boolean isBodyPart(Block block) {
        return this.body.blocks.contains(block);
    }

    /**
     * @return The world of the structure
     */
    public World getWorld() {
        return this.body.getWorld();
    }

    public abstract void destroyInventory();

    public boolean hasInventory() {
        return false;
    }

    /**
     * Checks if the structure is similar to the given structure
     *
     * @param structure The structure to check
     * @return true, if the structure is similar otherwise false
     */
    public boolean isSimilar(DAStructure structure) {
        return structure == this || structure.getBody().equals(this.getBody());
    }
}
