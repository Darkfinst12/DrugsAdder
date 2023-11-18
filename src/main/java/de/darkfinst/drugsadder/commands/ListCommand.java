package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.DADrug;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.items.DAItem;
import de.darkfinst.drugsadder.items.DAPlantItem;
import de.darkfinst.drugsadder.recipe.DARecipe;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class ListCommand {

    /**
     * Handels the list command with the given arguments and calls the required methods to list the items
     * <br>
     * args[0] = The type of the items, which should be listed
     * <br>
     * args[1] = The type of the recipes, which should be listed (only if args[0] = recipes)
     * <br>
     * Possible arguments: {@link PossibleArgs}
     *
     * @param commandSender The sender of the command
     * @param args          The arguments of the command
     */
    public static void execute(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length == 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_List"));
        } else {
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0]);
                if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
                }
                switch (possibleArgs) {
                    case CUSTOM_ITEMS -> ListCommand.customItems(commandSender);
                    case DRUGS -> ListCommand.drugs(commandSender);
                    case RECIPES -> ListCommand.recipes(commandSender, Arrays.copyOfRange(args, 1, args.length));
                    case PLANTS -> ListCommand.plants(commandSender);
                }
            } catch (Exception e) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_WrongArgs"));
            }
        }

    }

    /**
     * Handels the sub selection of the recipes to be listed and calls the correct method to list the recipes
     *
     * @param commandSender The sender of the command
     * @param args          The recipe type, which should be listed
     */
    private static void recipes(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_List_Recipes"));
        } else {
            PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0]);
            if (!commandSender.hasPermission(Objects.requireNonNull(possibleArgs).getPermission())) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
            }
            switch (possibleArgs) {
                case ALL ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.ALL, DAConfig.daRecipeReader.getRegisteredRecipes());
                case CRAFTER ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.CRAFTER, DAConfig.daRecipeReader.getCrafterRecipes());
                case CRAFTING ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.CRAFTING, DAConfig.daRecipeReader.getCraftingRecipes());
                case BARREL ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.BARREL, DAConfig.daRecipeReader.getBarrelRecipes());
                case PRESS ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.PRESS, DAConfig.daRecipeReader.getPressRecipes());
                case TABLE ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.TABLE, DAConfig.daRecipeReader.getTableRecipes());
                case FURNACE ->
                        ListCommand.listRecipes(commandSender, PossibleArgs.FURNACE, DAConfig.daRecipeReader.getFurnaceRecipes());
            }
        }
    }

    /**
     * Lists all registered recipes of the given type
     *
     * @param commandSender The sender of the command
     * @param type          The type of the recipes, which should be listed
     * @param recipes       The recipes, which should be listed
     */
    private static void listRecipes(CommandSender commandSender, PossibleArgs type, List<? extends DARecipe> recipes) {
        ListCommand.listItems(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_List_Recipes", type.getArg()), v -> recipes, DARecipe::asListComponent);
    }

    /**
     * Lists all registered drugs
     *
     * @param commandSender The sender of the command
     */
    private static void drugs(CommandSender commandSender) {
        if (!commandSender.hasPermission(PossibleArgs.DRUGS.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            ListCommand.listItems(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_List_Drugs"), v -> DAConfig.drugReader.getRegisteredDrugs(), DADrug::asListComponent);
        }
    }

    /**
     * Lists all registered plants
     *
     * @param commandSender The sender of the command
     */
    private static void plants(CommandSender commandSender) {
        if (!commandSender.hasPermission(PossibleArgs.DRUGS.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            ListCommand.listItems(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_List_Plants"), v -> DAConfig.seedReader.getRegisteredSeeds(), DAPlantItem::asListComponent);
        }
    }

    /**
     * Lists all registered custom items
     *
     * @param commandSender The sender of the command
     */
    private static void customItems(CommandSender commandSender) {
        if (!commandSender.hasPermission(PossibleArgs.CUSTOM_ITEMS.getPermission())) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            ListCommand.listItems(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_List_CustomItems"), v -> DAConfig.customItemReader.getRegisteredItems().values(), DAItem::asListComponent);
        }
    }

    /**
     * This method is used to list items of type T. It applies a function to get a collection of items,
     * converts each item to a Component using another function, and sends the resulting list as a message to a CommandSender.
     *
     * @param <T>             The type of items to be listed.
     * @param commandSender   The CommandSender to whom the list of items will be sent.
     * @param component       The initial Component to which the list of items will be appended.
     * @param getItems        A function that takes no arguments and returns a Collection of items of type T.
     * @param asListComponent A function that takes an item of type T and returns a Component representing that item.
     */
    private static <T> void listItems(CommandSender commandSender, Component component, Function<Void, Collection<T>> getItems, Function<T, Component> asListComponent) {
        for (T item : getItems.apply(null)) {
            component = component.appendNewline().append(Component.text("- ").append(asListComponent.apply(item)));
        }
        DA.loader.msg(commandSender, component);
    }

    /**
     * Manges the tab completion for the list command
     *
     * @param sender The sender of the command
     * @param args   The arguments of the command
     * @return A list of possible arguments
     */
    public static @NotNull List<String> complete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length <= 1) {
            return Arrays.stream(InfoCommand.PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getPos() == 0 && sender.hasPermission(possibleArgs.getPermission())).map(InfoCommand.PossibleArgs::getArg).filter(possArg -> possArg.toLowerCase().contains(args[0])).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getPosition() == 1 && sender.hasPermission(possibleArgs.getPermission())).map(PossibleArgs::getArg).filter(possArg -> possArg.toLowerCase().contains(args[1])).toList();
        }
        return new ArrayList<>();
    }

    /**
     * This enum contains all possible arguments for the list command
     */
    @Getter
    public enum PossibleArgs {
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.list.customitems", 0),
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.list.drugs", 0),
        RECIPES("Command_Arg_Recipes", "drugsadder.cmd.list.recipes", 0),
        PLANTS("Command_Arg_Plants", "drugsadder.cmd.list.plants", 0),

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
        private final int position;

        PossibleArgs(@NotNull String languageKey, @NotNull String permission, int position) {
            this.languageKey = languageKey;
            this.permission = permission;
            this.position = position;
        }

        public String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

        public static @Nullable PossibleArgs valueOfIgnoreCase(@Nullable String translation) {
            return Arrays.stream(PossibleArgs.values()).filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation)).findFirst().orElse(null);
        }

    }

}
