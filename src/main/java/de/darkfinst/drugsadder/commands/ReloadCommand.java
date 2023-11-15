package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
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
