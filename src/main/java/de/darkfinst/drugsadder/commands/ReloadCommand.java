package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand {

    /**
     * Handels the reload command and executes it
     *
     * @param commandSender The sender of the command
     * @param args          The arguments of the command – should be empty
     */
    public static void execute(@NotNull CommandSender commandSender, @NotNull String[] args) {
        if (args.length != 0) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Assistance_Reload"));
        } else if (!commandSender.hasPermission("drugsadder.cmd.reload")) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Error_NoPermission"));
        } else {
            DA.loader.reloadConfig();
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponentWithFallback("Command_Info_Reload"));
        }
    }


}
