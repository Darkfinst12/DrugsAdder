package de.darkfinst.drugsadder.filedata.readers;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class DACustomItemReader {

    @Getter(AccessLevel.NONE)
    private final ConfigurationSection config;
    private final Map<String, DAItem> registeredItems = new HashMap<>();
    private int configItemCount = 0;

    /**
     * This constructor is used to load the items from the config
     *
     * @param config The config to load the items from
     */
    public DACustomItemReader(ConfigurationSection config) {
        this.config = config;
    }

    public DACustomItemReader() {
        this.config = null;
    }

    /**
     * This method loads all items from the config
     */
    public void loadItems() {
        assert config != null;
        Set<String> items = config.getKeys(false);
        this.configItemCount = items.size();
        for (String itemID : items) {
            this.loadItem(itemID);
        }
        if (DAConfig.logCustomItemLoadComplete) {
            this.logInfo("Load_Info_CustomItemsComplete", (this.registeredItems.size() + ""), (this.configItemCount + ""));
        }
    }

    /**
     * This method loads a single item from the config
     *
     * @param itemID The ID of the item to load
     */
    private void loadItem(String itemID) {
        assert config != null;
        ConfigurationSection itemConfig = config.getConfigurationSection(itemID);
        String namespacedID = ("drugsadder:" + itemID).toLowerCase();
        if (itemConfig == null) {
            this.logError("Load_Error_CustomItem_NotConfigSection", itemID);
            return;
        }
        if (this.registeredItems.containsKey(namespacedID.toLowerCase())) {
            this.logError("Load_Error_CustomItem_IDAlreadyAssigned", itemID);
            return;
        }
        Material material = Material.getMaterial(itemConfig.getString("material", "AIR").toUpperCase());
        if (material == null || !material.isItem() || material.isAir() || Material.WATER.equals(material) || Material.LAVA.equals(material)) {
            this.logError("Load_Error_CustomItem_MaterialNotFound", itemID);
            return;
        }
        ItemStack itemStack = new ItemStack(material, 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        Component name = MiniMessage.miniMessage().deserialize(itemConfig.getString("name", itemID));
        itemMeta.displayName(name);
        List<Component> lore = new ArrayList<>();
        for (String loreLine : itemConfig.getStringList("lore")) {
            lore.add(MiniMessage.miniMessage().deserialize(loreLine));
        }
        itemMeta.lore(lore);
        int cmd = itemConfig.getInt("customModelData", -1);
        if (cmd >= 0) {
            itemMeta.setCustomModelData(cmd);
        }
        itemStack.setItemMeta(itemMeta);
        if (itemStack.getItemMeta() instanceof PotionMeta potionMeta) {
            potionMeta = this.loadPotionMeta(itemID, potionMeta, itemConfig);
            if (potionMeta == null) {
                return;
            } else {
                itemStack.setItemMeta(potionMeta);
            }
        } else {
            itemStack.setItemMeta(itemMeta);
        }
        DAItem item = new DAItem(itemStack, name, lore, cmd, namespacedID);
        item.setAmount(1);

        this.registeredItems.put(namespacedID, item);
        if (DAConfig.logCustomItemLoadInfo) {
            this.logInfo("Load_Info_CustomItem_Loaded", itemID);
        }
    }

    private PotionMeta loadPotionMeta(String itemID, PotionMeta potionMeta, ConfigurationSection itemConfig) {
        ConfigurationSection potionConfig = itemConfig.getConfigurationSection("potionMeta");
        if (potionConfig == null) {
            this.logError("Load_Error_CustomItem_PotionMeta", itemID);
            return null;
        }
        String argb = potionConfig.getString("color", "255,255,255,255");

        if (argb.split(",").length != 4) {
            this.logError("Load_Error_CustomItem_PotionColor", itemID);
        } else {
            argb = "255,255,255,255";
        }
        String[] argbSplit = argb.split(",");

        int alpha = Integer.parseInt(argbSplit[0]);
        int red = Integer.parseInt(argbSplit[1]);
        int green = Integer.parseInt(argbSplit[2]);
        int blue = Integer.parseInt(argbSplit[3]);
        potionMeta.setColor(Color.fromARGB(alpha, red, green, blue));

        potionConfig.getStringList("effects").forEach(effectString -> {
            PotionEffect potionEffect = this.loadPotionEffect(effectString);
            if (potionEffect != null) {
                potionMeta.addCustomEffect(potionEffect, true);
            }
        });

        return potionMeta;
    }


    private PotionEffect loadPotionEffect(String effectString) throws NumberFormatException {
        effectString = effectString.replace("PotionEffect{", "").replace("}", "");
        Map<String, String> map = DAUtil.parsMap(effectString);

        PotionEffectType effectType = PotionEffectType.getByName(Objects.requireNonNullElse(map.get("type"), "null"));
        int duration = Integer.parseInt(Objects.requireNonNullElse(map.get("duration"), "-1"));
        int level = Integer.parseInt(Objects.requireNonNullElse(map.get("level"), "-1"));
        if (effectType == null || duration == -1 || level == -1) {
            String effectName = Objects.requireNonNullElse(map.get("type"), "null");
            this.logError("Load_Error_CustomItem_PotionEffect", effectName);
            return null;
        }
        boolean particles = Boolean.parseBoolean(Objects.requireNonNullElse(map.get("particles"), "true"));
        boolean icon = Boolean.parseBoolean(Objects.requireNonNullElse(map.get("icon"), "true"));

        return new PotionEffect(effectType, duration, level, particles, icon);
    }

    /**
     * This method logs an error to the console
     *
     * @param key  The key of the error message in the language file
     * @param args The arguments for the error message (optional)
     */
    private void logError(String key, String... args) {
        if (DAConfig.logCustomItemLoadError) {
            LanguageReader languageReader = DA.loader.getLanguageReader();
            DALoader loader = DA.loader;
            if (languageReader != null) {
                loader.errorLog(languageReader.getString(key, args));
            } else {
                loader.errorLog("Error while loading CustomItem " + args[0] + " - Skipping");
            }
        }
    }

    /**
     * This method logs an info message to the console
     *
     * @param key  The key of the info message in the language file
     * @param args The arguments for the info message (optional)
     */
    private void logInfo(String key, String... args) {
        LanguageReader languageReader = DA.loader.getLanguageReader();
        DALoader loader = DA.loader;
        if (languageReader != null) {
            loader.log(languageReader.getString(key, args));
        }
    }

    /**
     * This method returns a copy of the item with the given namespaced ID
     *
     * @param namespacedID The namespaced ID of the item to get
     * @return The item with the given namespaced ID or null if no item with the given namespaced ID was found
     */
    public @Nullable DAItem getItemByNamespacedID(String namespacedID) {
        DAItem daItem = this.registeredItems.get(namespacedID);
        if (daItem != null) {
            daItem = daItem.clone();
        }
        return daItem;
    }

    /**
     * This method returns a copy of the item with the given namespaced ID
     *
     * @param itemStack The item to get
     * @return The found item or null if no item was found
     */
    public @Nullable DAItem getItemByItemStack(ItemStack itemStack) {
        return this.registeredItems.values().stream().filter(daItem -> DAUtil.matchItems(daItem.getItemStack(), itemStack, ItemMatchType.EXACT_CMD)).findFirst().orElse(null);
    }


    /**
     * This method returns a list of all registered items
     *
     * @return A list of all registered items or an empty list if no items were registered
     */
    public @NotNull List<String> getCustomItemNames() {
        List<String> names = new ArrayList<>();
        for (String key : this.registeredItems.keySet()) {
            names.add(key.replace("drugsadder:", ""));
        }
        return names;
    }
}
