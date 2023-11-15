package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DARecipe;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {


    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getPos() == 0 && sender.hasPermission(possibleArgs.getPermission())).map(PossibleArgs::getArg).filter(possArg -> possArg.toLowerCase().contains(args[0])).toList();
        } else if (args.length == 2) {
            if (PossibleArgs.RECIPES.getArg().equalsIgnoreCase(args[0]) && sender.hasPermission(PossibleArgs.RECIPES.getPermission())) {
                return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getPos() == 1 && sender.hasPermission(possibleArgs.getPermission())).map(PossibleArgs::getArg).filter(possArg -> possArg.toLowerCase().contains(args[1])).toList();
            } else if (PossibleArgs.CUSTOM_ITEMS.getArg().equalsIgnoreCase(args[0]) && sender.hasPermission(PossibleArgs.CUSTOM_ITEMS.getPermission())) {
                return DAConfig.customItemReader.getCustomItemNames().stream().filter(name -> name.toLowerCase().contains(args[1])).toList();
            } else if (PossibleArgs.DRUGS.getArg().equalsIgnoreCase(args[0]) && sender.hasPermission(PossibleArgs.DRUGS.getPermission())) {
                return DAConfig.drugReader.getDrugNames().stream().filter(name -> name.toLowerCase().contains(args[1])).toList();
            } else if (PossibleArgs.PLANT.getArg().equalsIgnoreCase(args[0]) && sender.hasPermission(PossibleArgs.PLANT.getPermission())) {
                return DAConfig.seedReader.getSeedNames().stream().filter(namespacedID -> namespacedID.contains(args[1])).toList();
            }
        } else if (args.length == 3 && PossibleArgs.RECIPES.getArg().equalsIgnoreCase(args[0]) && sender.hasPermission(PossibleArgs.RECIPES.getPermission())) {
            PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[1], 1);
            if (possibleArgs != null) {
                switch (possibleArgs) {
                    case ALL -> {
                        return DAConfig.daRecipeReader.getRegisteredRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case CRAFTER -> {
                        return DAConfig.daRecipeReader.getCrafterRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case CRAFTING -> {
                        return DAConfig.daRecipeReader.getCraftingRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case BARREL -> {
                        return DAConfig.daRecipeReader.getBarrelRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case PRESS -> {
                        return DAConfig.daRecipeReader.getPressRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case TABLE -> {
                        return DAConfig.daRecipeReader.getTableRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case FURNACE -> {
                        return DAConfig.daRecipeReader.getFurnaceRecipes().stream().map(DARecipe::getID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }

                }
            }
        }
        return new ArrayList<>();
    }

    //Enum for possible arguments
    @Getter
    public enum PossibleArgs {
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.list.customitems", 0),
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.list.drugs", 0),
        RECIPES("Command_Arg_Recipes", "drugsadder.cmd.list.recipes", 0),
        PLANT("Command_Arg_Plant", "drugsadder.cmd.list.plant", 0),

        ALL("Command_Arg_All", "drugsadder.cmd.list.all", 1),
        CRAFTER("Command_Arg_Crafter", "drugsadder.cmd.list.crafter", 1),
        CRAFTING("Command_Arg_Crafting", "drugsadder.cmd.list.crafting", 1),
        BARREL("Command_Arg_Barrel", "drugsadder.cmd.list.barrel", 1),
        PRESS("Command_Arg_Press", "drugsadder.cmd.list.press", 1),
        TABLE("Command_Arg_Table", "drugsadder.cmd.list.table", 1),
        FURNACE("Command_Arg_Furnace", "drugsadder.cmd.list.furnace", 1),
        ;

        private final String languageKey;
        private final String permission;
        private final int pos;

        PossibleArgs(String languageKey, String permission, int pos) {
            this.languageKey = languageKey;
            this.permission = permission;
            this.pos = pos;
        }

        public String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static PossibleArgs valueOfIgnoreCase(String translation, Integer pos) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .filter(possibleArgs -> pos == null || possibleArgs.getPos() == pos)
                    .findFirst()
                    .orElse(null);
        }

    }


}
