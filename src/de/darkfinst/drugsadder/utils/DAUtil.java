package de.darkfinst.drugsadder.utils;

import de.darkfinst.drugsadder.ItemMatchType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;

public class DAUtil {

    //Items
    public static boolean matchItems(ItemStack itemStackA, ItemStack itemStackB, ItemMatchType matchType) {
        switch (matchType) {
            case EXACT_CMD -> {
                return DAUtil.matchItemCMD(itemStackA, itemStackB);
            }
            case EXACT_NAME -> {
                return DAUtil.matchItemName(itemStackA, itemStackB);
            }
            case EXACT_LORE -> {
                return DAUtil.matchItemLore(itemStackA, itemStackB);
            }
            case CONTAINS_NAME -> {
                return DAUtil.matchItemNameContains(itemStackA, itemStackB);
            }
            case CONTAINS_LORE -> {
                return DAUtil.matchItemLoreContains(itemStackA, itemStackB);
            }
            default -> {
                return itemStackA.equals(itemStackB);
            }
        }
    }

    private static boolean matchItemLoreContains(ItemStack itemStackA, ItemStack itemStackB) {
        //TODO: Implement
        return false;
    }

    private static boolean matchItemNameContains(ItemStack itemStackA, ItemStack itemStackB) {
        //TODO: Implement
        return false;
    }

    private static boolean matchItemLore(ItemStack itemStackA, ItemStack itemStackB) {
        //TODO: Implement
        return false;
    }

    private static boolean matchItemName(ItemStack itemStackA, ItemStack itemStackB) {
        //TODO: Implement
        return false;
    }

    public static boolean matchItemCMD(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta() && itemStackA.getItemMeta().hasCustomModelData() && itemStackB.getItemMeta().hasCustomModelData() && itemStackA.getItemMeta().getCustomModelData() == itemStackB.getItemMeta().getCustomModelData();
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
