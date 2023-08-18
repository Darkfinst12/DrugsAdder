package de.darkfinst.drugsadder.filedata;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.items.DAItem;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public class DACustomItemReader {

    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
    private final List<DAItem> registeredItems = new ArrayList<>();
    private int configItemCount = 0;

    public DACustomItemReader(ConfigurationSection config) {
        this.config = config;
    }

    public DACustomItemReader() {
        this.config = null;
    }

    public void loadItems() {
        assert config != null;
        Set<String> items = config.getKeys(false);
        this.configItemCount = items.size();
        for (String itemID : items) {
            this.loadItem(itemID);
        }
        if (DAConfig.logCustomItemLoadComplete) {
            this.logInfo("Load_Info_DrugsComplete", (this.registeredItems.size() + ""), (this.configItemCount + ""));
        }
    }

    private void loadItem(String itemID) {
        assert config != null;
        ConfigurationSection itemConfig = config.getConfigurationSection(itemID);
        String namespacedID = ("drugsadder:" + itemID);
        if (itemConfig == null) {
            this.logError("Load_Error_CustomItem_NotConfigSection", itemID);
            return;
        }
        if (this.registeredItems.stream().anyMatch(daItem -> daItem.getNamespacedID().toLowerCase().equals(namespacedID))) {
            this.logError("Load_Error_CustomItem_IDAlreadyAssigned", itemID);
            return;
        }
        Material material = Material.getMaterial(itemConfig.getString("material", "AIR").toUpperCase());
        if (material == null || !material.isItem() || material.isAir() || Material.WATER.equals(material) || Material.LAVA.equals(material)) {
            this.logError("Load_Error_CustomItem_MaterialNotFound", itemID);
            return;
        }
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        String name = ChatColor.translateAlternateColorCodes('&', itemConfig.getString("name", itemID));
        itemMeta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        for (String loreLine : itemConfig.getStringList("lore")) {
            lore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
        }
        itemMeta.setLore(lore);
        int cmd = itemConfig.getInt("customModelData", -1);
        if (cmd >= 0) {
            itemMeta.setCustomModelData(cmd);
        }
        itemStack.setItemMeta(itemMeta);
        DAItem item = new DAItem(itemStack, name, lore, cmd, namespacedID);

        this.registeredItems.add(item);
        if (DAConfig.logCustomItemLoadInfo) {
            this.logInfo("Load_Info_CustomItem_Loaded", itemID);
        }
    }

    public DAItem getItemByNamespacedID(String namespacedID) {
        return this.registeredItems.stream().filter(daItem -> daItem.getNamespacedID().equalsIgnoreCase(namespacedID)).findFirst().orElse(null);
    }

    private void logError(String key, String... args) {
        if (DAConfig.logCustomItemLoadError) {
            LanguageReader languageReader = DA.loader.getLanguageReader();
            DALoader loader = DA.loader;
            if (languageReader != null) {
                loader.errorLog(languageReader.get(key, args));
            } else {
                loader.errorLog("Error while loading CustomItem " + args[0] + " - Skipping");
            }
        }
    }

    private void logInfo(String key, String... args) {
        LanguageReader languageReader = DA.loader.getLanguageReader();
        DALoader loader = DA.loader;
        if (languageReader != null) {
            loader.log(languageReader.get(key, args));
        }
    }
}
