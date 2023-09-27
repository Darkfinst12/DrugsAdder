package de.darkfinst.drugsadder.utils;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DALoader;
import de.darkfinst.drugsadder.ItemMatchType;
import de.darkfinst.drugsadder.exceptions.DamageToolException;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import dev.lone.itemsadder.api.CustomStack;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
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

    /**
     * Gets the DAItem by the namespaced id
     *
     * @param namespacedID The namespaced id of the item
     * @return The DAItem or null if the item was not found
     */
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

    /**
     * Gets the namespaced id of the item
     *
     * @param item The item to get the namespaced id from
     * @return The namespaced id of the item
     */
    public static @NotNull String getNamespacedIDByItemStack(@NotNull ItemStack item) {
        DAItem daItem = DAConfig.customItemReader.getItemByItemStack(item);
        if (daItem != null) {
            return daItem.getNamespacedID();
        }
        if (DAConfig.hasItemsAdder) {
            CustomStack customStack = CustomStack.byItemStack(item);
            if (customStack != null) {
                customStack = CustomStack.getInstance(customStack.getNamespacedID());
                return customStack.getNamespacedID();
            }
        }
        if (DAConfig.hasSlimefun) {
            SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
            if (slimefunItem != null) {
                return slimefunItem.getId();
            }
        }
        return item.getType().getKey().toString();
    }

    /**
     * Gets the default item of the item, but only for custom items
     * <br>
     * If the Item has a lore or a display name, it will be removed and replaced with the default values
     *
     * @param item The item to get the default item from
     * @return The default item of the item or null if the item is not a custom item
     */
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

    /**
     * Sets on a slot the default item of the item, but only for custom items
     *
     * @param event The event to get the inventory from
     * @param slot  The slot to set the default item
     */
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

    /**
     * Matches two items with the given match types
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @param matchTypes The match types
     * @return If the items match
     */
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

    /**
     * Matches two items with the given match type
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @param matchType  The match type
     * @return If the items match, according to the match type
     */
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

    /**
     * Matches two items with their lore
     * <br>
     * The lore of the item must contain the lore of the other item
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @return If the items match
     */
    private static boolean matchItemLoreContains(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        if (itemStackA.hasItemMeta() && itemStackB.hasItemMeta() && itemStackA.getItemMeta().hasLore() && itemStackB.getItemMeta().hasLore()) {
            return new HashSet<>(itemStackB.getItemMeta().getLore()).containsAll(itemStackA.getItemMeta().getLore()) || new HashSet<>(itemStackA.getItemMeta().getLore()).containsAll(itemStackB.getItemMeta().getLore());
        }
        return false;
    }

    /**
     * Matches two items with their name
     * <br>
     * The name of the item must contain the name of the other item
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @return If the items match
     */
    private static boolean matchItemNameContains(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().hasDisplayName() && itemStackB.getItemMeta().hasDisplayName()
                && (itemStackA.getItemMeta().getDisplayName().contains(itemStackB.getItemMeta().getDisplayName())
                || itemStackB.getItemMeta().getDisplayName().contains(itemStackA.getItemMeta().getDisplayName()));
    }

    /**
     * Matches two items with their Lore
     * <br>
     * The lore of the item must be the same as the lore of the other item
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @return If the items match
     */
    private static boolean matchItemLore(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        if (itemStackA.hasItemMeta() && itemStackB.hasItemMeta() && itemStackA.getItemMeta().hasLore() && itemStackB.getItemMeta().hasLore()) {
            return new HashSet<>(itemStackB.getItemMeta().getLore()).equals(new HashSet<>(itemStackA.getItemMeta().getLore()));
        }
        return false;
    }

    /**
     * Matches two items with their name
     * <br>
     * The name of the item must be the same as the name of the other item
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @return If the items match
     */
    private static boolean matchItemName(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().getDisplayName().equals(itemStackB.getItemMeta().getDisplayName());
    }

    /**
     * Matches two items with their custom model data
     * <br>
     * The custom model data of the item must be the same as the custom model data of the other item
     *
     * @param itemStackA The first item
     * @param itemStackB The second item
     * @return If the items match
     */
    public static boolean matchItemCMD(@NotNull ItemStack itemStackA, @NotNull ItemStack itemStackB) {
        return itemStackA.hasItemMeta() && itemStackB.hasItemMeta()
                && itemStackA.getItemMeta().hasCustomModelData() && itemStackB.getItemMeta().hasCustomModelData()
                && itemStackA.getItemMeta().getCustomModelData() == itemStackB.getItemMeta().getCustomModelData();
    }

    /**
     * Damages the given item stack if it is damageable
     * <p>
     * If it is damageable, it executes {@link DAUtil#applyDamage(ItemStack, int)}
     *
     * @param itemStack The item stack to damage
     * @param damage    The damage to apply
     * @return The damaged item stack
     * @throws DamageToolException If the item stack is not damageable
     */
    public static ItemStack damageTool(ItemStack itemStack, int damage) throws DamageToolException {
        if (itemStack == null || itemStack.getType().isAir()) {
            return itemStack;
        }
        if (damage > 0) {
            boolean applyDamage;
            if (itemStack.getEnchantments().containsKey(Enchantment.DURABILITY)) {
                int level = itemStack.getEnchantments().get(Enchantment.DURABILITY);
                float random = DA.secureRandom.nextFloat();
                float applyChance = 100f / (level + 1);
                applyDamage = (random * 100) <= applyChance;
            } else {
                applyDamage = true;
            }
            if (applyDamage) {
                if (DAUtil.canApplyDamage(itemStack, damage)) {
                    return DAUtil.applyDamage(itemStack, damage);
                } else {
                    throw new DamageToolException();
                }
            }
        }
        return itemStack;
    }

    /**
     * Applies the damage to the item stack
     *
     * @param itemStack The item stack to apply the damage to
     * @param damage    The damage to apply
     * @return The damaged item stack or null if the item stack is broken
     */
    private static @Nullable ItemStack applyDamage(@NotNull ItemStack itemStack, int damage) {
        ItemStack returnItem = itemStack.clone();
        ItemMeta itemMeta = returnItem.getItemMeta();
        if (CustomStack.byItemStack(itemStack) != null) {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            int newDurability = customStack.getDurability() - damage;
            if (newDurability <= 0) {
                returnItem = null;
            } else {
                customStack.setDurability(newDurability);
                returnItem = customStack.getItemStack();
            }
        } else if (itemMeta instanceof Damageable damageable) {
            int newDamage = damageable.getDamage() + damage;
            if (newDamage >= itemStack.getType().getMaxDurability()) {
                returnItem = null;
            } else {
                damageable.setDamage(newDamage);
                returnItem.setItemMeta(itemMeta);
            }
        }
        return returnItem;
    }

    /**
     * Checks if the item stack can be damaged
     *
     * @param itemStack The item stack to check
     * @param damage    The damage to apply
     * @return If the item stack can be damaged
     */
    private static boolean canApplyDamage(ItemStack itemStack, int damage) {
        boolean canBeApplied = false;

        if (CustomStack.byItemStack(itemStack) != null) {
            CustomStack customStack = CustomStack.byItemStack(itemStack);
            int newDurability = customStack.getDurability() - damage;
            canBeApplied = newDurability >= 0;
        } else if (itemStack.getItemMeta() instanceof Damageable damageable) {
            int newDurability = damageable.getDamage() + damage;
            canBeApplied = newDurability <= itemStack.getType().getMaxDurability();
        }


        return canBeApplied;
    }


    //Parse

    /**
     * Creates a Map with key value pairs from a string like "key1=value1,key2=value2"
     *
     * @param string The string to parse
     * @return The map with the key value pairs
     */
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

    /**
     * Saves a file from the resources to the dest file
     *
     * @param in        The input stream of the file
     * @param dest      The destination file
     * @param name      The name of the file
     * @param overwrite If the file should be overwritten
     * @throws IOException If an error occurs while saving the file
     */
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

    /**
     * Creates the world sections in the config
     *
     * @param section The section to create the world sections in
     */
    public static void createWorldSections(ConfigurationSection section) {
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getUID().toString();
            section.createSection(worldName);
        }
    }

    //Pars

    /**
     * Parses a string to an integer
     *
     * @param string The string to parse
     * @return The parsed integer or 0 if the string is null or not a number
     */
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

    /**
     * Parses a string to a double
     *
     * @param string The string to parse
     * @return The parsed double or 0 if the string is null or not a number
     */
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

    /**
     * Parses a string to a float
     *
     * @param string The string to parse
     * @return The parsed float or 0 if the string is null or not a number
     */
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

    /**
     * Finds the closest value to the target in the array
     *
     * @param arr    The array to search in
     * @param target The target to search for
     * @return The closest value to the target in the array or the target if it is in the array
     */
    public static int findClosest(int[] arr, int target) {
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

    /**
     * Gets the closest value to the target
     *
     * @param val1   The first value
     * @param val2   The second value
     * @param target The target
     * @return The closest value to the target
     */
    public static int getClosest(int val1, int val2,
                                 int target) {
        if (target - val1 >= val2 - target)
            return val2;
        else
            return val1;
    }

    //Spaces

    /**
     * Note this works only with the <a href="https://github.com/AmberWat/NegativeSpaceFont/tree/master">NegativeSpaceFont</a> from <a href="https://github.com/AmberWat">AmberWat</a>
     *
     * <br>
     * <table>
     *     <tr>
     *         <th>Width</th>
     *         <th>Minecraft Code</th>
     *     </tr>
     *     <tr>
     *         <td>8192</td>
     *         <td>\#uDB08\#uDC00</td>
     *     </tr>
     *     <tr>
     *         <td>...</td>
     *         <td>...</td>
     *     </tr>
     *     <tr>
     *         <td>1</td>
     *         <td>\#uDB00\#uDC01</td>
     *     </tr>
     *     <tr>
     *         <td>0</td>
     *         <td>\#uDB00\#uDC00</td>
     *     </tr>
     *     <tr>
     *         <td>-1</td>
     *         <td>\#uDAFF\#uDFFF</td>
     *     </tr>
     *     <tr>
     *         <td>...</td>
     *         <td>...</td>
     *     </tr>
     *     <tr>
     *         <td>-8192</td>
     *         <td>\#uDAF8\#uDC00</td>
     *     </tr>
     * </table>
     * Ignore the # in the code it is only there to prevent the code from being converted
     *
     * @param width The width of the space
     * @return The needed Minecraft Code
     */
    public static String convertWidthToMinecraftCode(int width) {
        if (width < -8192 || width > 8192) {
            throw new IllegalArgumentException("Width must be between -8192 and 8192");
        }
        int code = 0xD0000 + width;
        int highSurrogate = 0xD800 + ((code - 0x10000) >> 10);
        int lowSurrogate = 0xDC00 + ((code - 0x10000) & 0x3FF);
        return new String(new int[]{highSurrogate, lowSurrogate}, 0, 2);
    }

}
