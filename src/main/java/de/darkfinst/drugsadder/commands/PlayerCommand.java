package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {

    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

    @Getter
    private enum PossibleArgs {
        SET("Command_Args_Set", "drugsadder.cmd.player.set"),
        GET("Command_Args_Get", "drugsadder.cmd.player.get"),
        ADD("Command_Args_Add", "drugsadder.cmd.player.add"),
        REMOVE("Command_Args_Remove", "drugsadder.cmd.player.remove"),
        CLEAR("Command_Args_Clear", "drugsadder.cmd.player.clear"),
        INFO("Command_Args_Info", "drugsadder.cmd.player.info"),
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
