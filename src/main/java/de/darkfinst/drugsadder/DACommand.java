package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.*;
import de.darkfinst.drugsadder.utils.DAUtil;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DACommand implements CommandExecutor, TabCompleter {

    private static final List<String> MAIN_ARGS = List.of(PossibleArgs.RELOAD.getArg(), PossibleArgs.GET_CUSTOM_ITEM.getArg(), PossibleArgs.LIST.getArg(), PossibleArgs.CONSUME.getArg());
    private static final List<String> LIST_ARGS = List.of(PossibleArgs.RECIPES.getArg(), PossibleArgs.DRUGS.getArg(), PossibleArgs.CUSTOM_ITEMS.getArg());
    private static final List<String> LIST_RECIPES_ARGS = List.of(PossibleArgs.ALL.getArg(), PossibleArgs.BARREL.getArg(), PossibleArgs.CRAFTING.getArg(), PossibleArgs.FURNACE.getArg(), PossibleArgs.PRESS.getArg(), PossibleArgs.TABLE.getArg());


    public void register() {
        PluginCommand command = DA.getInstance.getCommand("drugsadder");
        assert command != null;
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            this.checkArgs1(commandSender, args);
        } else if (args.length == 2) {
            this.checkArgs2(commandSender, args);
        } else if (args.length == 3) {
            this.checkArgs3(commandSender, args);
        }
        return true;
    }

    private void checkArgs1(CommandSender commandSender, String[] args) {
        this.checkReload(commandSender, args);
    }

    private void checkReload(CommandSender commandSender, String[] args) {
        DALoader loader = DA.loader;
        if (args[0].equalsIgnoreCase(PossibleArgs.RELOAD.getArg())) {
            loader.reloadConfig();
            commandSender.sendMessage("Reloaded config");
        }
    }

    private void checkArgs2(CommandSender commandSender, String[] args) {
        this.checkCustomItem(commandSender, args);
        this.checkConsume(commandSender, args);
    }

    private void checkCustomItem(CommandSender commandSender, String[] args) {
        if (args[0].equalsIgnoreCase(PossibleArgs.GET_CUSTOM_ITEM.getArg())) {
            if (commandSender instanceof Player player) {
                DAItem customItem = DAUtil.getItemStackByNamespacedID("drugsadder:" + args[1]);
                if (customItem != null) {
                    player.getInventory().addItem(customItem.getItemStack());
                } else {
                    CustomStack customStack = CustomStack.getInstance(args[1]);
                    if (customStack != null) {
                        player.getInventory().addItem(customStack.getItemStack());
                    } else {
                        commandSender.sendMessage("CustomItem not found + " + args[1]);
                    }
                }
            } else {
                commandSender.sendMessage(DA.loader.getTranslation("You need to be a Player to execute this Command", "Command_Error_NotPlayer"));
            }
        }
    }

    private void checkList(CommandSender commandSender, String[] args) {
        if (args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg())) {
            this.checkListRecipes(commandSender, args);
            this.checkListDrugs(commandSender, args);
            if (args[1].equalsIgnoreCase(PossibleArgs.CUSTOM_ITEMS.getArg())) {
                if (args[2].equalsIgnoreCase(PossibleArgs.OWN.getArg())) {
                    DA.loader.msg(commandSender, DAConfig.customItemReader.getCustomItemNames().toString());
                } else if (args[2].equalsIgnoreCase(PossibleArgs.OTHER.getArg())) {
                    DA.loader.msg(commandSender, CustomStack.getNamespacedIdsInRegistry().toString());
                }
            }
        }
    }

    private void checkListRecipes(CommandSender commandSender, String[] args) {
        if (args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            if (args[2].equalsIgnoreCase(PossibleArgs.ALL.getArg())) {
                for (DARecipe registeredRecipe : DAConfig.daRecipeReader.getRegisteredRecipes()) {
                    DA.loader.msg(commandSender, registeredRecipe.toString());
                }
            } else if (args[2].equalsIgnoreCase(PossibleArgs.BARREL.getArg())) {
                for (DABarrelRecipe barrelRecipe : DAConfig.daRecipeReader.getBarrelRecipes()) {
                    DA.loader.msg(commandSender, barrelRecipe.toString());
                }
            } else if (args[2].equalsIgnoreCase(PossibleArgs.CRAFTING.getArg())) {
                for (DACraftingRecipe craftingRecipe : DAConfig.daRecipeReader.getCraftingRecipes()) {
                    DA.loader.msg(commandSender, craftingRecipe.toString());
                }
            } else if (args[2].equalsIgnoreCase(PossibleArgs.FURNACE.getArg())) {
                for (DAFurnaceRecipe furnaceRecipe : DAConfig.daRecipeReader.getFurnaceRecipes()) {
                    DA.loader.msg(commandSender, furnaceRecipe.toString());
                }
            } else if (args[2].equalsIgnoreCase(PossibleArgs.PRESS.getArg())) {
                for (DAPressRecipe pressRecipe : DAConfig.daRecipeReader.getPressRecipes()) {
                    DA.loader.msg(commandSender, pressRecipe.toString());
                }
            } else if (args[2].equalsIgnoreCase(PossibleArgs.TABLE.getArg())) {
                for (DATableRecipe tableRecipe : DAConfig.daRecipeReader.getTableRecipes()) {
                    DA.loader.msg(commandSender, tableRecipe.toString());
                }
            }
        }
    }

    private void checkListDrugs(CommandSender commandSender, String[] args) {
        if (args[1].equalsIgnoreCase(PossibleArgs.DRUGS.getArg())) {
            DADrug daDrug = DAConfig.drugReader.getDrug(args[2]);
            if (daDrug != null) {
                DA.loader.msg(commandSender, daDrug.toString());
            } else {
                commandSender.sendMessage("Drug not found");
            }
        }
    }

    private void checkConsume(CommandSender commandSender, String[] args) {
        if (args[0].equalsIgnoreCase(PossibleArgs.CONSUME.getArg())) {
            DADrug daDrug = DAConfig.drugReader.getDrug(args[1]);
            if (daDrug == null) {
                commandSender.sendMessage(DA.loader.getTranslation("Drug not found", "Command_Error_DrugNotFound", args[2]));
                return;
            }
            Player target = args.length == 3 ? Bukkit.getPlayer(args[2]) : (commandSender instanceof Player player ? player : null);
            if (target == null) {
                commandSender.sendMessage(DA.loader.getTranslation("Player not found", "Command_Error_PlayerNotFound", args[1]));
                return;
            }
            daDrug.consume(target);
        }
    }

    private void checkArgs3(CommandSender commandSender, String[] args) {
        this.checkList(commandSender, args);
        this.checkConsume(commandSender, args);
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command
            command, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            return MAIN_ARGS.stream().filter(s1 -> s1.contains(args[0])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.GET_CUSTOM_ITEM.getArg())) {
            return DAConfig.customItemReader.getCustomItemNames().stream().filter(s1 -> s1.contains(args[1])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg())) {
            return LIST_ARGS.stream().filter(s1 -> s1.contains(args[1])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.CONSUME.getArg())) {
            if (!args[1].isEmpty() || !args[1].isBlank()) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s1 -> s1.contains(args[1])).toList();
            } else {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            return LIST_RECIPES_ARGS.stream().filter(s1 -> s1.contains(args[2])).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.CONSUME.getArg())) {
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
        CONSUME(DA.loader.getTranslation("consume", "Command_Args_Consume")),
        RECIPES(DA.loader.getTranslation("recipes", "Command_Args_Recipes")),
        DRUGS(DA.loader.getTranslation("drugs", "Command_Args_Drugs")),
        ALL(DA.loader.getTranslation("all", "Command_Args_All")),
        BARREL(DA.loader.getTranslation("barrel", "Command_Args_Barrel")),
        CRAFTING(DA.loader.getTranslation("crafting", "Command_Args_Crafting")),
        FURNACE(DA.loader.getTranslation("furnace", "Command_Args_Furnace")),
        PRESS(DA.loader.getTranslation("press", "Command_Args_Press")),
        TABLE(DA.loader.getTranslation("table", "Command_Args_Table")),
        OWN(DA.loader.getTranslation("own", "Command_Args_Own")),
        OTHER(DA.loader.getTranslation("other", "Command_Args_Other")),
        CUSTOM_ITEMS(DA.loader.getTranslation("customItems", "Command_Args_CustomItems")),
        ;
        private final String arg;

        PossibleArgs(String arg) {
            this.arg = arg;
        }

    }
}
