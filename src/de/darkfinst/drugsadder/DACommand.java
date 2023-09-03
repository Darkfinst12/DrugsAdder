package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DACraftingRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class DACommand implements CommandExecutor, TabCompleter {

    public void register() {
        PluginCommand command = DA.getInstance.getCommand("drugsadder");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        DALoader loader = DA.loader;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                loader.reloadConfig();
                commandSender.sendMessage("Reloaded config");
            } else if (args[0].equalsIgnoreCase("testProperties") && commandSender instanceof Player player) {

                try {
                    PlayerProfile playerProfile = player.getPlayerProfile();
                    if (playerProfile instanceof org.bukkit.craftbukkit.v1_20_R1.profile.CraftPlayerProfile craftPlayerProfile) {
                        DA.log.log(craftPlayerProfile.toString());
                    } /*
                    else if (playerProfile instanceof com.destroystokyo.paper.profile.CraftPlayerProfile craftPlayerProfile) {
                        DA.log.log(craftPlayerProfile.toString());
                    }
                    */ else {
                        DA.log.log("PlayerProfile is not CraftPlayerProfile it is " + playerProfile.getClass().getName());
                    }
                } catch (Exception e) {
                    DA.log.logException(e);
                } finally {
                    player.sendMessage("Check console");
                }
            }
        } else if (args.length == 2 && commandSender instanceof Player player) {
            if (args[0].equalsIgnoreCase("getCustomItem")) {
                DAItem customItem = DAConfig.customItemReader.getItemByNamespacedID("drugsadder:" + args[1]);
                if (customItem != null) {
                    player.getInventory().addItem(customItem.getItemStack());
                } else {
                    player.sendMessage("Custom item not found");
                }
            }
        } else if (args.length == 3 && commandSender instanceof Player player) {
            if (args[0].equalsIgnoreCase("list")) {
                if (args[1].equalsIgnoreCase("recipes")) {
                    if (args[2].equalsIgnoreCase("all")) {
                        for (DARecipe registeredRecipe : DAConfig.daRecipeReader.getRegisteredRecipes()) {
                            StringBuilder stringBuilder = new StringBuilder("Recipe: ");
                            stringBuilder.append(registeredRecipe.getRecipeNamedID());
                            stringBuilder.append(" - Type: ");
                            stringBuilder.append(registeredRecipe.getClass().getSimpleName());
                            stringBuilder.append(" - Result: ");
                            stringBuilder.append(registeredRecipe.getResult().getNamespacedID());
                            stringBuilder.append(" - Materials: ");
                            for (DAItem material : registeredRecipe.getMaterials()) {
                                stringBuilder.append(material.getNamespacedID());
                                stringBuilder.append(", ");
                            }
                            DA.loader.msg(player, stringBuilder.toString());
                        }
                    } else if (args[2].equalsIgnoreCase("barrel")) {

                    } else if (args[2].equalsIgnoreCase("crafting")) {
                        var list = DAConfig.daRecipeReader.getRegisteredRecipes().stream().filter(daRecipe -> daRecipe instanceof DACraftingRecipe).toList();
                        for (DARecipe daRecipe : list) {
                            if (daRecipe instanceof DACraftingRecipe craftingRecipe) {
                                StringBuilder stringBuilder = new StringBuilder("CraftingRecipe: ");
                                stringBuilder.append(craftingRecipe.getNamedID());
                                stringBuilder.append(" - Result: ");
                                stringBuilder.append(" - ShapeType: ");
                                stringBuilder.append(craftingRecipe.isShapeless() ? "Shapeless" : "Shaped");
                                stringBuilder.append(" - Shape: ");
                                for (String shape : craftingRecipe.getShape()) {
                                    stringBuilder.append(shape);
                                    stringBuilder.append(", ");
                                }
                                stringBuilder.append(craftingRecipe.getResult().getNamespacedID());
                                stringBuilder.append(" - Materials: ");
                                for (int i = 0; i < craftingRecipe.getShapeKeys().size() - 1; i++) {
                                    String key = craftingRecipe.getShapeKeys().keySet().toArray(new String[0])[i];
                                    stringBuilder.append(key);
                                    stringBuilder.append(": ");
                                    stringBuilder.append(craftingRecipe.getMaterials()[i].getNamespacedID());
                                    if (i < craftingRecipe.getShapeKeys().size() - 1) {
                                        stringBuilder.append(", ");
                                    }
                                }
                                stringBuilder.append(" - BukkitRegistered: ");
                                NamespacedKey namespacedKey = new NamespacedKey(DA.getInstance, craftingRecipe.getNamedID());
                                stringBuilder.append(Bukkit.getRecipe(namespacedKey) != null);
                                DA.loader.msg(player, stringBuilder.toString());
                            }
                        }
                    } else if (args[2].equalsIgnoreCase("furnace")) {

                    } else if (args[2].equalsIgnoreCase("press")) {

                    } else if (args[2].equalsIgnoreCase("table")) {

                    }
                } else if (args[1].equalsIgnoreCase("drugs")) {
                    DADrug daDrug = DAConfig.drugReader.getDrug(args[2]);
                    if (daDrug != null) {
                        DA.loader.msg(player, daDrug.toString());
                    } else {
                        player.sendMessage("Drug not found");
                    }
                }
            } else if (args[0].equalsIgnoreCase("setAddiction")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    commandSender.sendMessage("Player not found");
                    return true;
                }
                DADrug daDrug = DAConfig.drugReader.getDrug(args[2]);
                if (daDrug == null) {
                    commandSender.sendMessage("Drug not found");
                    return true;
                }
                daDrug.consume(target);
            }
        }
        return true;
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command
            command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            return Stream.of("reload", "getCustomItem", "list", "testProperties", "setAddiction").filter(s1 -> s1.contains(strings[0])).toList();
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("getCustomItem")) {
            return DAConfig.customItemReader.getCustomItemNames().stream().filter(s1 -> s1.contains(strings[1])).toList();
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("list")) {
            return Stream.of("recipes", "drugs").filter(s1 -> s1.contains(strings[1])).toList();
        }
        if (strings.length == 2 && strings[0].equalsIgnoreCase("setAddiction")) {
            if (!strings[1].isEmpty()) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s1 -> s1.contains(strings[1])).toList();
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (strings.length == 3 && strings[0].equalsIgnoreCase("list") && strings[1].equalsIgnoreCase("recipes")) {
            return Stream.of("all", "barrel", "crafting", "furnace", "press", "table").filter(s1 -> s1.contains(strings[2])).toList();
        }
        if (strings.length == 3 && strings[0].equalsIgnoreCase("setAddiction")) {
            var drugs = DAConfig.drugReader.getRegisteredDrugs().stream().filter(drug -> drug.getID().contains(strings[2])).toList();
            return drugs.stream().map(DADrug::getID).toList();
        }
        if (strings.length == 3 && strings[0].equalsIgnoreCase("list") && strings[1].equalsIgnoreCase("drugs")) {
            return DAConfig.drugReader.getRegisteredDrugs().stream().map(DADrug::getID).toList();
        }
        return null;
    }
}
