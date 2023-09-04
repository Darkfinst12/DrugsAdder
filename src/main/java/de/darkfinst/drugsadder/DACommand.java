package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.DACraftingRecipe;
import de.darkfinst.drugsadder.recipe.DARecipe;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class DACommand implements CommandExecutor, TabCompleter {

    private static final Stream<String> MAIN_ARGS = Stream.of(PossibleArgs.RELOAD.getArg(), PossibleArgs.GET_CUSTOM_ITEM.getArg(), PossibleArgs.LIST.getArg(), PossibleArgs.SET_ADDICTION.getArg());
    private static final Stream<String> LIST_ARGS = Stream.of(PossibleArgs.RECIPES.getArg(), PossibleArgs.DRUGS.getArg());
    private static final Stream<String> LIST_RECIPES_ARGS = Stream.of(PossibleArgs.ALL.getArg(), PossibleArgs.BARREL.getArg(), PossibleArgs.CRAFTING.getArg(), PossibleArgs.FURNACE.getArg(), PossibleArgs.PRESS.getArg(), PossibleArgs.TABLE.getArg());


    public void register() {
        PluginCommand command = DA.getInstance.getCommand("drugsadder");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
        DALoader loader = DA.loader;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase(PossibleArgs.RELOAD.getArg())) {
                loader.reloadConfig();
                commandSender.sendMessage("Reloaded config");
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
            if (args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg())) {
                if (args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
                    if (args[2].equalsIgnoreCase(PossibleArgs.ALL.getArg())) {
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
                    } else if (args[2].equalsIgnoreCase(PossibleArgs.BARREL.getArg())) {

                    } else if (args[2].equalsIgnoreCase(PossibleArgs.CRAFTING.getArg())) {
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
                    } else if (args[2].equalsIgnoreCase(PossibleArgs.FURNACE.getArg())) {

                    } else if (args[2].equalsIgnoreCase(PossibleArgs.PRESS.getArg())) {

                    } else if (args[2].equalsIgnoreCase(PossibleArgs.TABLE.getArg())) {

                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.DRUGS.getArg())) {
                    DADrug daDrug = DAConfig.drugReader.getDrug(args[2]);
                    if (daDrug != null) {
                        DA.loader.msg(player, daDrug.toString());
                    } else {
                        player.sendMessage("Drug not found");
                    }
                }
            } else if (args[0].equalsIgnoreCase(PossibleArgs.SET_ADDICTION.getArg())) {
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
            command, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            return MAIN_ARGS.filter(s1 -> s1.contains(args[0])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.GET_CUSTOM_ITEM.getArg())) {
            return DAConfig.customItemReader.getCustomItemNames().stream().filter(s1 -> s1.contains(args[1])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg())) {
            return LIST_ARGS.filter(s1 -> s1.contains(args[1])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.SET_ADDICTION.getArg())) {
            if (!args[1].isEmpty() || !args[1].isBlank()) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s1 -> s1.contains(args[1])).toList();
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            return LIST_RECIPES_ARGS.filter(s1 -> s1.contains(args[2])).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.SET_ADDICTION.getArg())) {
            var drugs = DAConfig.drugReader.getRegisteredDrugs().stream().filter(drug -> drug.getID().contains(args[2])).toList();
            return drugs.stream().map(DADrug::getID).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.DRUGS.getArg())) {
            return DAConfig.drugReader.getRegisteredDrugs().stream().map(DADrug::getID).toList();
        }
        return null;
    }

    @Getter
    public enum PossibleArgs {
        RELOAD(DA.loader.getTranslation("reload", "Command_Args_Reload")),
        GET_CUSTOM_ITEM(DA.loader.getTranslation("getCustomItem", "Command_Args_GetCustomItem")),
        LIST(DA.loader.getTranslation("list", "Command_Args_List")),
        SET_ADDICTION(DA.loader.getTranslation("setAddiction", "Command_Args_SetAddiction")),
        RECIPES(DA.loader.getTranslation("recipes", "Command_Args_Recipes")),
        DRUGS(DA.loader.getTranslation("drugs", "Command_Args_Drugs")),
        ALL(DA.loader.getTranslation("all", "Command_Args_All")),
        BARREL(DA.loader.getTranslation("barrel", "Command_Args_Barrel")),
        CRAFTING(DA.loader.getTranslation("crafting", "Command_Args_Crafting")),
        FURNACE(DA.loader.getTranslation("furnace", "Command_Args_Furnace")),
        PRESS(DA.loader.getTranslation("press", "Command_Args_Press")),
        TABLE(DA.loader.getTranslation("table", "Command_Args_Table"));

        private final String arg;

        PossibleArgs(String arg) {
            this.arg = arg;
        }

    }
}
