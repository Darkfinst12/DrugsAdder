package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
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

        public static ListCommand.PossibleArgs valueOfIgnoreCase(String translation) {
            return Arrays.stream(ListCommand.PossibleArgs.values())
                    .filter(possibleArgs -> possibleArgs.getArg().equalsIgnoreCase(translation))
                    .findFirst()
                    .orElse(null);
        }

    }


}
