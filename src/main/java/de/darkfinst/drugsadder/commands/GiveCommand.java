package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import de.darkfinst.drugsadder.filedata.DAConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GiveCommand {

    public static void execute(@NotNull CommandSender commandSender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage(DA.loader.languageReader.getComponent("Command_Assistance_Reload"));
        } else if (!commandSender.hasPermission("drugsadder.cmd.give")) {
            DA.loader.msg(commandSender, DA.loader.languageReader.getComponent("Command_Error_NoPermission"));
        } else {
            //TODO: Implement
        }
    }


    public static @NotNull List<String> complete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return DAConfig.customItemReader.getCustomItemNames().stream().filter(itemName -> itemName.toLowerCase().contains(args[0])).toList();
    }


}
