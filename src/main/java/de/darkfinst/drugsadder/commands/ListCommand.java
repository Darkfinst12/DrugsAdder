package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ListCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {

    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of(PossibleArgs.CUSTOM_TIMES.getArg(), PossibleArgs.DRUGS.getArg(), PossibleArgs.RECIPES.getArg());
        }
        return new ArrayList<>();
    }

    //Enum for possible arguments
    @Getter
    private enum PossibleArgs {
        CUSTOM_TIMES("Command_Arg_CustomItems", "drugsadder.cmd.list.customitems"),
        DRUGS("Command_Arg_Drugs", "drugsadder.cmd.list.drugs"),
        RECIPES("Command_Arg_Recipes", "drugsadder.cmd.list.recipes"),
        ALL("Command_Arg_All", "drugsadder.cmd.list.all"),
        CRAFTER("Command_Arg_Crafter", "drugsadder.cmd.list.crafter"),
        CRAFTING("Command_Arg_Crafting", "drugsadder.cmd.list.crafting"),
        BARREL("Command_Arg_Barrel", "drugsadder.cmd.list.barrel"),
        PRESS("Command_Arg_Press", "drugsadder.cmd.list.press"),
        TABLE("Command_Arg_Table", "drugsadder.cmd.list.table"),
        FURNACE("Command_Arg_Furnace", "drugsadder.cmd.list.furnace"),
        ;

        private final String languageKey;
        private final String permission;

        PossibleArgs(String languageKey, String permission) {
            this.languageKey = languageKey;
            this.permission = permission;
        }

        public String getArg() {
            return DA.loader.languageReader.getString(languageKey);
        }

    }

}
