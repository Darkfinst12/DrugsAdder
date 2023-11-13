package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import de.darkfinst.drugsadder.recipe.DARecipe;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Assistance_List"));
        } else {
            try {
                PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0]);
                if (!commandSender.hasPermission(possibleArgs.getPermission())) {
                    DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Error_NoPermission"));
                }
                switch (possibleArgs) {
                    case CUSTOM_ITEMS -> ListCommand.customItems(commandSender);
                    case DRUGS -> ListCommand.drugs(commandSender);
                    case RECIPES -> ListCommand.recipes(commandSender, Arrays.copyOfRange(args, 1, args.length));
                }
            } catch (Exception e) {
                DA.log.logException(e);
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Error_WrongArgs"));
            }
        }

    }

    private static void recipes(CommandSender commandSender, String[] args) {
        if (args.length != 1) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Assistance_List_Recipes"));
        } else {
            PossibleArgs possibleArgs = PossibleArgs.valueOfIgnoreCase(args[0]);
            if (!commandSender.hasPermission(possibleArgs.getPermission())) {
                DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Error_NoPermission"));
            }
            switch (possibleArgs) {
                case ALL -> ListCommand.allRecipes(commandSender);
                case CRAFTER -> ListCommand.crafterRecipes(commandSender);
                case CRAFTING -> ListCommand.craftingRecipes(commandSender);
                case BARREL -> ListCommand.barrelRecipes(commandSender);
                case PRESS -> ListCommand.pressRecipes(commandSender);
                case TABLE -> ListCommand.tableRecipes(commandSender);
                case FURNACE -> ListCommand.furnaceRecipes(commandSender);
            }
        }
    }

    private static void allRecipes(CommandSender commandSender) {
        Component component = DA.loader.languageReader.getComponent("Command_List_Recipes", PossibleArgs.ALL.getArg());
        for (DARecipe registeredRecipe : DAConfig.daRecipeReader.getRegisteredRecipes()) {
            component = component.appendNewline().append(Component.text("- ").append(registeredRecipe.asComponent()));
        }
        commandSender.sendMessage(component);
    }

    private static void crafterRecipes(CommandSender commandSender) {
    }

    private static void craftingRecipes(CommandSender commandSender) {
    }

    private static void barrelRecipes(CommandSender commandSender) {
    }

    private static void pressRecipes(CommandSender commandSender) {
    }

    private static void tableRecipes(CommandSender commandSender) {
    }

    private static void furnaceRecipes(CommandSender commandSender) {
    }

    private static void drugs(CommandSender commandSender) {
    }

    private static void customItems(CommandSender commandSender) {

    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of(PossibleArgs.CUSTOM_ITEMS.getArg(), PossibleArgs.DRUGS.getArg(), PossibleArgs.RECIPES.getArg());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase(PossibleArgs.RECIPES.getArg())) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getPos() == 1 && sender.hasPermission(possibleArgs.getPermission()))
                    .map(PossibleArgs::getArg)
                    .filter(possArg -> possArg.toLowerCase().contains(args[1]))
                    .toList();
        }
        return new ArrayList<>();
    }

    //Enum for possible arguments
    @Getter
    public enum PossibleArgs {
        CUSTOM_ITEMS("Command_Arg_CustomItems", "drugsadder.cmd.list.customitems", 0),
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.list.drugs", 0),
        RECIPES("Command_Arg_Recipes", "drugsadder.cmd.list.recipes", 0),

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

        public static PossibleArgs valueOfIgnoreCase(String translation) {
            return Arrays.stream(PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .findFirst()
                    .orElse(null);
        }

    }

}
