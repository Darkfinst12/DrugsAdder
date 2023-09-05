package de.darkfinst.drugsadder.structures;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.structures.barrel.DABarrel;
import de.darkfinst.drugsadder.structures.press.DAPress;
import de.darkfinst.drugsadder.structures.table.DATable;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Setter
@Getter
public abstract class DAStructure {


    private DABody body;

    public boolean isBodyPart(Block block) {
        return this.body.blocks.contains(block);
    }

    public World getWorld() {
        return this.body.getWorld();
    }

    public static void save(ConfigurationSection config, ConfigurationSection oldData) {
        DAUtil.createWorldSections(config);

        if (!DA.loader.getStructureList().isEmpty()) {
            int barrelID = 0;
            int pressID = 0;
            int tableID = 0;
            for (DAStructure structure : DA.loader.getStructureList()) {
                if (structure instanceof DABarrel barrel) {
                    String worldName = barrel.getWorld().getUID().toString();
                    String prefix = worldName + "." + "barrels." + barrelID;

                    Location loc = barrel.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

                    int slot = 0;
                    ItemStack item;
                    ConfigurationSection invConfig = null;
                    while (slot < barrel.getInventory().getSize()) {
                        item = barrel.getInventory().getItem(slot);
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


                    barrelID++;
                } else if (structure instanceof DAPress press) {
                    String worldName = press.getWorld().getUID().toString();
                    String prefix = worldName + "." + "presses." + pressID;

                    Location loc = press.getBody().getSign().getLocation();
                    config.set(prefix + ".sign", loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());

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

                    int slot = 0;
                    ItemStack item;
                    ConfigurationSection invConfig = null;
                    while (slot < table.getInventory().getSize()) {
                        item = table.getInventory().getItem(slot);
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
                    tableID++;
                }
            }
        }

        if (oldData != null) {
            DA.log.debugLog("Merging old data into new data");
            for (String world : oldData.getKeys(false)) {
                DA.log.debugLog("Merging old data for " + world);
                ConfigurationSection structureSection = oldData.getConfigurationSection(world);
                for (String structure : structureSection.getKeys(false)) {
                    ConfigurationSection structureData = structureSection.getConfigurationSection(structure);
                    for (String key : structureData.getKeys(false)) {
                        if (!config.contains(world + "." + structure + "." + key)) {
                            config.set(world + "." + structure + "." + key, oldData.get(world + "." + structure + "." + key));
                            DA.log.debugLog("Merging old data for " + world + "." + structure + "." + key);
                        } else {
                            DA.log.debugLog("Not merging old data for " + world + "." + structure + "." + key + " because it already exists");
                        }
                    }
                }
            }
        } else {
            DA.log.debugLog("No old data to merge");
        }
    }

    public void destroyInventory() {
    }

    public boolean hasInventory() {
        return false;
    }
}
