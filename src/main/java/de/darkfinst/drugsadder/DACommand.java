package de.darkfinst.drugsadder;

import de.darkfinst.drugsadder.api.events.DrugsAdderSendMessageEvent;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.recipe.*;
import de.darkfinst.drugsadder.utils.DAUtil;
import dev.lone.itemsadder.api.CustomStack;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DACommand implements CommandExecutor, TabCompleter {

    private static final List<String> MAIN_ARGS = List.of(PossibleArgs.RELOAD.getArg(), PossibleArgs.GET_CUSTOM_ITEM.getArg(), PossibleArgs.LIST.getArg(), PossibleArgs.CONSUME.getArg(), PossibleArgs.INFO.getArg());
    private static final List<String> LIST_ARGS = List.of(PossibleArgs.RECIPES.getArg(), PossibleArgs.DRUGS.getArg(), PossibleArgs.CUSTOM_ITEMS.getArg());
    private static final List<String> LIST_RECIPES_ARGS = List.of(PossibleArgs.ALL.getArg(), PossibleArgs.BARREL.getArg(), PossibleArgs.CRAFTING.getArg(), PossibleArgs.FURNACE.getArg(), PossibleArgs.PRESS.getArg(), PossibleArgs.TABLE.getArg());
    private static final List<String> LIST_INFO_ARGS = List.of(PossibleArgs.DRUGS.getArg(), PossibleArgs.BARREL.getArg(), PossibleArgs.CRAFTING.getArg(), PossibleArgs.FURNACE.getArg(), PossibleArgs.PRESS.getArg(), PossibleArgs.TABLE.getArg(), PossibleArgs.CUSTOM_ITEMS.getArg(), PossibleArgs.PLAYER.getArg());


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
        this.checkInfo(commandSender, args);
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
                String recipes = this.getRecipeList(DAConfig.daRecipeReader.getRegisteredRecipes(), PossibleArgs.RECIPES);
                DA.loader.msg(commandSender, recipes);
            } else if (args[2].equalsIgnoreCase(PossibleArgs.BARREL.getArg())) {
                String recipes = this.getRecipeList(DAConfig.daRecipeReader.getBarrelRecipes(), PossibleArgs.BARREL);
                DA.loader.msg(commandSender, recipes);
            } else if (args[2].equalsIgnoreCase(PossibleArgs.CRAFTING.getArg())) {
                String recipes = getRecipeList(DAConfig.daRecipeReader.getCraftingRecipes(), PossibleArgs.CRAFTING);
                DA.loader.msg(commandSender, recipes);
            } else if (args[2].equalsIgnoreCase(PossibleArgs.FURNACE.getArg())) {
                String recipes = this.getRecipeList(DAConfig.daRecipeReader.getFurnaceRecipes(), PossibleArgs.FURNACE);
                DA.loader.msg(commandSender, recipes);
            } else if (args[2].equalsIgnoreCase(PossibleArgs.PRESS.getArg())) {
                String recipes = this.getRecipeList(DAConfig.daRecipeReader.getPressRecipes(), PossibleArgs.PRESS);
                DA.loader.msg(commandSender, recipes);
            } else if (args[2].equalsIgnoreCase(PossibleArgs.TABLE.getArg())) {
                String recipes = this.getRecipeList(DAConfig.daRecipeReader.getTableRecipes(), PossibleArgs.TABLE);
                DA.loader.msg(commandSender, recipes);
            }
        }
    }

    private @NotNull String getRecipeList(List<?> recipeList, PossibleArgs recipeType) {
        StringBuilder recipes = new StringBuilder(DA.loader.languageReader.get("Command_Info_ListItems", recipeType.getArg()) + "\n");
        for (Object o : recipeList) {
            DARecipe registeredRecipe = (DARecipe) o;
            String s = "- ID:" + registeredRecipe.getNamedID() + " - Type: " + registeredRecipe.getRecipeType() + "\n";
            recipes.append(s);
        }
        return recipes.toString();
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
        this.checkInfo(commandSender, args);
    }

    private void checkInfo(CommandSender commandSender, String[] args) {
        if (args[0].equalsIgnoreCase(PossibleArgs.INFO.getArg())) {
            if (args.length == 1) {
                String version = DA.getInstance.getDescription().getVersion();
                String authors = DA.getInstance.getDescription().getAuthors().toString().replace("[", "").replace("]", "");
                DA.loader.msg(commandSender, DA.loader.languageReader.get("Command_Info_DAInfo", version, authors), DrugsAdderSendMessageEvent.Type.COMMAND);
            } else if (args.length == 2) {
                if (args[1].equalsIgnoreCase(PossibleArgs.PLAYER.getArg()) && commandSender instanceof Player player) {
                    DAPlayer daPlayer = DA.loader.getDaPlayer(player);
                    if (daPlayer != null) {
                        DA.loader.msg(commandSender, daPlayer.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_PlayerNotFound", player.getName());
                    }
                }
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase(PossibleArgs.DRUGS.getArg())) {
                    DADrug daDrug = DAConfig.drugReader.getDrug(args[2]);
                    if (daDrug != null) {
                        DA.loader.msg(commandSender, daDrug.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_DrugNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
                    DARecipe daRecipe = DAConfig.daRecipeReader.getRecipe(args[2]);
                    if (daRecipe != null) {
                        DA.loader.msg(commandSender, daRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.BARREL.getArg())) {
                    DABarrelRecipe daBarrelRecipe = DAConfig.daRecipeReader.getBarrelRecipe(args[2]);
                    if (daBarrelRecipe != null) {
                        DA.loader.msg(commandSender, daBarrelRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.CRAFTING.getArg())) {
                    DACraftingRecipe daCraftingRecipe = DAConfig.daRecipeReader.getCraftingRecipe(args[2]);
                    if (daCraftingRecipe != null) {
                        DA.loader.msg(commandSender, daCraftingRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.FURNACE.getArg())) {
                    DAFurnaceRecipe daFurnaceRecipe = DAConfig.daRecipeReader.getFurnaceRecipe(args[2]);
                    if (daFurnaceRecipe != null) {
                        DA.loader.msg(commandSender, daFurnaceRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.PRESS.getArg())) {
                    DAPressRecipe daPressRecipe = DAConfig.daRecipeReader.getPressRecipe(args[2]);
                    if (daPressRecipe != null) {
                        DA.loader.msg(commandSender, daPressRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                } else if (args[1].equalsIgnoreCase(PossibleArgs.TABLE.getArg())) {
                    DATableRecipe daTableRecipe = DAConfig.daRecipeReader.getTableRecipe(args[2]);
                    if (daTableRecipe != null) {
                        DA.loader.msg(commandSender, daTableRecipe.toString());
                    } else {
                        DA.loader.languageReader.get("Command_Error_RecipeNotFound", args[2]);
                    }
                }
            }
        }
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command
            command, @NotNull String commandLabel, @NotNull String[] args) {
        if (args.length == 1) {
            return MAIN_ARGS.stream().filter(s1 -> s1.toLowerCase().contains(args[0].toLowerCase())).toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg())) {
            return LIST_ARGS.stream().filter(s1 -> s1.toLowerCase().contains(args[1].toLowerCase())).toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.INFO.getArg())) {
            return LIST_INFO_ARGS.stream().filter(s1 -> s1.toLowerCase().contains(args[1].toLowerCase())).toList();
        } else if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.CONSUME.getArg())) {
            return DAConfig.drugReader.getDrugNames().stream().filter(s1 -> s1.toLowerCase().contains(args[1])).toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            return LIST_RECIPES_ARGS.stream().filter(s1 -> s1.toLowerCase().contains(args[2].toLowerCase())).toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.DRUGS.getArg())) {
            return DAConfig.drugReader.getDrugNames().stream().filter(s1 -> s1.toLowerCase().contains(args[2].toLowerCase())).toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.CUSTOM_ITEMS.getArg())) {
            return DAConfig.customItemReader.getCustomItemNames().stream().filter(s1 -> s1.toLowerCase().contains(args[2].toLowerCase())).toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.LIST.getArg()) && args[1].equalsIgnoreCase(PossibleArgs.PLAYER.getArg())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().stream().filter(s1 -> s1.toLowerCase().contains(args[2].toLowerCase())).toList();
        } else if (args.length == 3 && args[0].equalsIgnoreCase(PossibleArgs.CONSUME.getArg())) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList().stream().filter(s1 -> s1.toLowerCase().contains(args[2].toLowerCase())).toList();
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
        INFO(DA.loader.getTranslation("info", "Command_Args_Info")),
        PLAYER(DA.loader.getTranslation("player", "Command_Args_Player"));
        private final String arg;

        PossibleArgs(String arg) {
            this.arg = arg;
        }

    }
}
