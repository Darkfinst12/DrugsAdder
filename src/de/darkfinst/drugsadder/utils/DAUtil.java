package de.darkfinst.drugsadder.utils;

import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DAUtil {

    //Items
    public static DAItem getItemStackByNamespacedID(String namespacedID) {
        DAItem daItem = DAConfig.customItemReader.getItemByNamespacedID(namespacedID);
        if (daItem != null) {
            return daItem;
        }
        //TODO: Support: MMOItems - https://gitlab.com/phoenix-dvpmt/mmoitems
        //TODO: Support: Slimefun4 - https://github.com/Slimefun/Slimefun4
        //TODO: Support: ItemsAdder - https://github.com/LoneDev6/API-ItemsAdder
        return null;
    }

    public static boolean matchItems(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB, @NotNull ItemMatchType matchType) {
        boolean match = false;
        if (itemStackA.getType().equals(itemStackB.getType())) {
            switch (matchType) {
                case EXACT_CMD -> match = DAUtil.matchItemCMD(itemStackA, itemStackB);
                case EXACT_NAME -> match = DAUtil.matchItemName(itemStackA, itemStackB);
                case EXACT_LORE -> match = DAUtil.matchItemLore(itemStackA, itemStackB);
                case CONTAINS_NAME -> match = DAUtil.matchItemNameContains(itemStackA, itemStackB);
                case CONTAINS_LORE -> match = DAUtil.matchItemLoreContains(itemStackA, itemStackB);
                case ALL -> match = itemStackA.equals(itemStackB);
                default -> match = true;
            }
        }
        return match;
    }

    private static boolean matchItemLoreContains(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        if (itemStackA.hasItemMeta() && itemStackB.hasItemMeta() && itemStackA.getItemMeta().hasLore() && itemStackB.getItemMeta().hasLore()) {
            return new HashSet<>(itemStackB.getItemMeta().getLore()).containsAll(itemStackA.getItemMeta().getLore()) || new HashSet<>(itemStackA.getItemMeta().getLore()).containsAll(itemStackB.getItemMeta().getLore());
        }
        return false;
    }

    private static boolean matchItemNameContains(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().hasDisplayName() && itemStackB.getItemMeta().hasDisplayName()
                && (itemStackA.getItemMeta().getDisplayName().contains(itemStackB.getItemMeta().getDisplayName())
                || itemStackB.getItemMeta().getDisplayName().contains(itemStackA.getItemMeta().getDisplayName()));
    }

    private static boolean matchItemLore(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        if (itemStackA.hasItemMeta() && itemStackB.hasItemMeta() && itemStackA.getItemMeta().hasLore() && itemStackB.getItemMeta().hasLore()) {
            return new HashSet<>(itemStackB.getItemMeta().getLore()).equals(new HashSet<>(itemStackA.getItemMeta().getLore()));
        }
        return false;
    }

    private static boolean matchItemName(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().getDisplayName().equals(itemStackB.getItemMeta().getDisplayName());
    }

    public static boolean matchItemCMD(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().hasCustomModelData() && itemStackB.getItemMeta().hasCustomModelData()
                && itemStackA.getItemMeta().getCustomModelData() == itemStackB.getItemMeta().getCustomModelData();
    }

    //Parse
    @NotNull
    public static Map<String, String> parsMap(@NotNull String string) {
        String[] keyValues = string.split(",");
        HashMap<String, String> map = new HashMap<>();

        for (String keyValue : keyValues) {
            String[] split = keyValue.split("=");
            if (split.length != 2) {
                continue;
            }
            map.put(split[0].trim(), split[1].trim());
        }
        return map;
    }

    //Config
    public static boolean verify(String name, Class type, FileConfiguration config) {
        return !config.contains(name) && (config.get(name) == null || !type.isInstance(config.get(name)));
    }

    public static boolean verify(String name, Class type, ConfigurationSection config) {
        return !config.contains(name) && (config.get(name) == null || !type.isInstance(config.get(name)));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveFile(InputStream in, File dest, String name, boolean overwrite) throws IOException {
        if (in == null) return;
        if (!dest.exists()) {
            dest.mkdirs();
        }
        File result = new File(dest, name);
        if (result.exists()) {
            if (overwrite) {
                result.delete();
            } else {
                return;
            }
        }

        OutputStream out = Files.newOutputStream(result.toPath());
        byte[] buffer = new byte[1024];

        int length;
        //copy the file content in bytes
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }

        in.close();
        out.close();
    }

}
