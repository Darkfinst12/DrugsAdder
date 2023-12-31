package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.recipe.DARecipe;
import de.darkfinst.drugsadder.utils.DAUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InfoCommand {

    /**
     * Handles the info command with the given arguments and calls the corresponding method for the given argument
     * <p>
     * Possible arguments: {@link PossibleArgs}
     *
     * @param commandSender The command sender
     * @param args          The arguments
     */
    public static void execute(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length == 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Info"));
        } else {
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0], 0);
                if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
                }
                switch (possibleArgs) {
                    case CUSTOM_ITEMS ->
                            InfoCommand.customItems(commandSender, Arrays.copyOfRange(args, 1, args.length));
                    case DRUGS -> InfoCommand.drugs(commandSender, Arrays.copyOfRange(args, 1, args.length));
                    case RECIPES -> InfoCommand.recipes(commandSender, Arrays.copyOfRange(args, 1, args.length));
                    case PLANT -> InfoCommand.plant(commandSender, Arrays.copyOfRange(args, 1, args.length));
                    case PLUGIN -> InfoCommand.plugin(commandSender);
                }
            } catch (Exception e) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_WrongArgs"));
            }
        }
    }

    /**
     * Gives the information about the given custom item to the command sender
     *
     * @param commandSender The command sender
     * @param args          The custom item ID or namespacedID
     */
    private static void customItems(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (!commandSender.hasPermission(PossibleArgs.CUSTOM_ITEMS.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            String namespacedID = args[0].contains(":") ? args[0] : "drugsadder:" + args[0];
            DAItem daItem = DAUtil.getItemStackByNamespacedID(namespacedID);
            if (daItem == null) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_CustomItemNotFound", namespacedID));
            } else {
                Component component = DA.loader.languageReader.getComponentWithFallback("Command_Info_CustomItem");
                component = component.appendNewline().append(daItem.asInfoComponent());
                DA.loader.msg(commandSender, component);
            }
        }
    }

    /**
     * Gives the information about the given drug to the command sender
     *
     * @param commandSender The command sender
     * @param args          The ID of the drug
     */
    private static void drugs(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (!commandSender.hasPermission(PossibleArgs.DRUGS.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            DADrug daDrug = DAConfig.drugReader.getDrug(args[0]);
            if (daDrug == null) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_DrugNotFound", args[0]));
            } else {
                Component component = DA.loader.languageReader.getComponentWithFallback("Command_Info_Drug");
                component = component.appendNewline().append(daDrug.asInfoComponent());

                DA.loader.msg(commandSender, component);
            }
        }
    }

    /**
     * Gives the information about the given plant to the command sender
     *
     * @param commandSender The command sender
     * @param args          The ID of the plant
     */
    private static void plant(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (!commandSender.hasPermission(PossibleArgs.PLANT.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            DAPlantItem daPlant = DAConfig.seedReader.getSeed(args[0]);
            if (daPlant == null) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_PlantNotFound", args[0]));
            } else {
                Component component = DA.loader.languageReader.getComponentWithFallback("Command_Info_Plant");
                component = component.appendNewline().append(daPlant.asInfoComponent());

                DA.loader.msg(commandSender, component);
            }
        }
    }

    /**
     * Gives the information about the plugin to the command sender
     *
     * @param commandSender The command sender
     */
    private static void plugin(@NotNull CommandSender commandSender) {
        DA.loader.msg(commandSender, DA.getInstance.getInfoComponent());
    }

    /**
     * Handles the sub selection of the recipe to get information about
     *
     * @param commandSender The command sender
     * @param args          The type of the recipe and the ID of the recipe
     */
    private static void recipes(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (!commandSender.hasPermission(PossibleArgs.RECIPES.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0], 1);
                if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
                }
                switch (possibleArgs) {
                    case ALL ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getRecipe(args[1]), args[1]);
                    case CRAFTER ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getCrafterRecipe(args[1]), args[1]);
                    case CRAFTING ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getCraftingRecipe(args[1]), args[1]);
                    case BARREL ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getBarrelRecipe(args[1]), args[1]);
                    case PRESS ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getPressRecipe(args[1]), args[1]);
                    case TABLE ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getTableRecipe(args[1]), args[1]);
                    case FURNACE ->
                            InfoCommand.listRecipe(commandSender, DAConfig.daRecipeReader.getFurnaceRecipe(args[1]), args[1]);
                }
            } catch (Exception e) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_WrongArgs"));
            }
        }
    }

    /**
     * Gives the information about the given recipe to the command sender
     *
     * @param commandSender The command sender
     * @param recipe        The recipe – can be null
     * @param recipeID      The ID of the recipe
     */
    private static void listRecipe(@NotNull CommandSender commandSender, @Nullable DARecipe recipe, @NotNull String recipeID) {
        if (recipe == null) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_RecipeNotFound", recipeID));
        } else {
            Component component = DA.loader.languageReader.getComponentWithFallback("Command_Info_Recipe", recipe.getRecipeType().name());
            component = component.appendNewline().append(recipe.asInfoComponent());

            DA.loader.msg(commandSender, component);
        }
    }

    /**
     * Manges tge tab completion for the info command
     *
     * @param sender The command sender
     * @param args   The arguments of the command
     * @return A list of possible arguments
     */

    public static @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
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
                        return DAConfig.daRecipeReader.getRegisteredRecipes().stream().map(DARecipe::getRecipeNamedID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case CRAFTER -> {
                        return DAConfig.daRecipeReader.getCrafterRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case CRAFTING -> {
                        return DAConfig.daRecipeReader.getCraftingRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case BARREL -> {
                        return DAConfig.daRecipeReader.getBarrelRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case PRESS -> {
                        return DAConfig.daRecipeReader.getPressRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case TABLE -> {
                        return DAConfig.daRecipeReader.getTableRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }
                    case FURNACE -> {
                        return DAConfig.daRecipeReader.getFurnaceRecipes().stream().map(DARecipe::getRecipeID).filter(namespacedID -> namespacedID.contains(args[2])).toList();
                    }

                }
            }
        }
        return new ArrayList<>();
    }

    /**
     * This enum contains all possible arguments for the info command.
     */
    @Getter
    public enum PossibleArgs {
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.info.customitems", 0),
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.info.drugs", 0),
        RECIPES("Command_Arg_Recipes", "drugsadder.cmd.info.recipes", 0),
        PLANT("Command_Arg_Plant", "drugsadder.cmd.info.plant", 0),
        PLUGIN("Command_Arg_Plugin", "drugsadder.cmd.info.plugin", 0),

        ALL("Command_Arg_All", "drugsadder.cmd.info.recipes.all", 1),
        CRAFTER("Command_Arg_Crafter", "drugsadder.cmd.info.recipes.crafter", 1),
        CRAFTING("Command_Arg_Crafting", "drugsadder.cmd.info.recipes.crafting", 1),
        BARREL("Command_Arg_Barrel", "drugsadder.cmd.info.recipes.barrel", 1),
        PRESS("Command_Arg_Press", "drugsadder.cmd.info.recipes.press", 1),
        TABLE("Command_Arg_Table", "drugsadder.cmd.info.recipes.table", 1),
        FURNACE("Command_Arg_Furnace", "drugsadder.cmd.info.recipes.furnace", 1),
        ;

        private final String languageKey;
        private final String permission;
        private final int pos;

        PossibleArgs(@NotNull String languageKey, @NotNull String permission, int pos) {
            this.languageKey = languageKey;
            this.permission = permission;
            this.pos = pos;
        }

        public @NotNull String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static @Nullable PossibleArgs valueOfIgnoreCase(@NotNull String translation, @Nullable Integer pos) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .filter(possibleArgs -> pos == null || possibleArgs.getPos() == pos)
                    .findFirst()
                    .orElse(null);
        }

    }

}
