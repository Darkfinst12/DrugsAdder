package de.darkfinst.drugsadder.utils;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import dev.lone.itemsadder.api.CustomStack;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DAUtil {

    //Items
    public static @Nullable DAItem getItemStackByNamespacedID(@NotNull String namespacedID) {
        if (namespacedID.startsWith("minecraft:")) {
            Material material = Material.getMaterial(namespacedID.split(":")[1].toUpperCase());
            if (material != null) {
                return new DAItem(new ItemStack(material, 1), namespacedID);
            }
        } else {
            DAItem item = DAConfig.customItemReader.getItemByNamespacedID(namespacedID);
            if (item != null) {
                return item;
            }
            if (DAConfig.hasItemsAdder) {
                if (DALoader.iaLoaded) {
                    CustomStack customStack = CustomStack.getInstance(namespacedID);
                    if (customStack != null) {
                        return new DAItem(customStack.getItemStack(), namespacedID);
                    }
                } else {
                    ItemStack itemStack = new ItemStack(Material.STICK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§cItemsAdder not loaded");
                    itemStack.setItemMeta(itemMeta);
                    return new DAItem(itemStack, namespacedID);
                }
            }
            if (DAConfig.hasSlimefun) {
                SlimefunItem slimefunItem = SlimefunItem.getById(namespacedID.split(":")[1]);
                if (slimefunItem != null) {
                    return new DAItem(slimefunItem.getItem(), namespacedID);
                }
            }
        }
        return null;
    }

    public static @Nullable ItemStack getDefaultItem(ItemStack item) {
        DAItem daItem = DAConfig.customItemReader.getItemByItemStack(item);
        if (daItem != null) {
            return daItem.getItemStack();
        }
        if (DAConfig.hasItemsAdder) {
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack != null) {
                customStack = CustomStack.getInstance(customStack.getNamespacedID());
                return customStack.getItemStack();
            }
        }
        if (DAConfig.hasSlimefun) {
            SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
            if (slimefunItem != null) {
                return slimefunItem.getItem();
            }
        }
        return null;
    }

    public static void setSlotDefaultItem(InventoryEvent event, Integer slot) {
        Bukkit.getScheduler().runTaskLater(DA.getInstance, () -> {
            ItemStack ogItem = event.getInventory().getItem(slot);
            if (ogItem != null) {
                ItemStack itemStack = DAUtil.getDefaultItem(ogItem);
                if (itemStack != null) {
                    itemStack.setAmount(ogItem.getAmount());
                    event.getInventory().setItem(slot, itemStack);
                }
            }
        }, 1L);
    }

    public static boolean matchItems(ItemStack itemStackA, ItemStack itemStackB, ItemMatchType... matchTypes) {
        boolean match = false;
        if (!(itemStackA == null || itemStackB == null || matchTypes == null || matchTypes.length == 0)) {
            for (ItemMatchType matchType : matchTypes) {
                match = DAUtil.matchItems(itemStackA, itemStackB, matchType);
                if (!match) {
                    break;
                }
            }
        }
        return match;
    }

    public static boolean matchItems(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB, @NotNull ItemMatchType matchType) {
        boolean match = false;
        ItemStack stackA = itemStackA.clone();
        ItemStack stackB = itemStackB.clone();
        stackA.setAmount(1);
        stackB.setAmount(1);
        if (itemStackA.getType().equals(itemStackB.getType())) {
            switch (matchType) {
                case EXACT_CMD -> match = DAUtil.matchItemCMD(stackA, stackB);
                case EXACT_NAME -> match = DAUtil.matchItemName(stackA, stackB);
                case EXACT_LORE -> match = DAUtil.matchItemLore(stackA, stackB);
                case CONTAINS_NAME -> match = DAUtil.matchItemNameContains(stackA, stackB);
                case CONTAINS_LORE -> match = DAUtil.matchItemLoreContains(stackA, stackB);
                case VANNILA -> match = stackA.isSimilar(stackB);
                case ALL -> match = stackA.equals(stackB);
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
    public static @NotNull Map<String, String> parsMap(@NotNull String string) {
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

    public static void createWorldSections(ConfigurationSection section) {
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getUID().toString();
            section.createSection(worldName);
        }
    }

    //Pars
    public static int parseInt(String string) {
        if (string == null) {
            return 0;
        }
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static double parseDouble(String string) {
        if (string == null) {
            return 0;
        }
        try {
            return Double.parseDouble(string);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static float parseFloat(String string) {
        if (string == null) {
            return 0;
        }
        try {
            return Float.parseFloat(string);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }


    //Binary Search
    public static int findClosest(int arr[], int target) {
        int n = arr.length;

        if (target <= arr[0])
            return arr[0];
        if (target >= arr[n - 1])
            return arr[n - 1];

        int i = 0, j = n, mid = 0;
        while (i < j) {
            mid = (i + j) / 2;

            if (arr[mid] == target)
                return arr[mid];

            if (target < arr[mid]) {
                if (mid > 0 && target > arr[mid - 1])
                    return getClosest(arr[mid - 1],
                            arr[mid], target);

                j = mid;
            } else {
                if (mid < n - 1 && target < arr[mid + 1])
                    return getClosest(arr[mid],
                            arr[mid + 1], target);
                i = mid + 1; // update i
            }
        }

        return arr[mid];
    }

    public static int getClosest(int val1, int val2,
                                 int target) {
        if (target - val1 >= val2 - target)
            return val2;
        else
            return val1;
    }

}
