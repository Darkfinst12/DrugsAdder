package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DACommandManager implements CommandExecutor, TabCompleter {

    public static final String COMMAND_NAME = "drugsadder";

    public void register() {
        PluginCommand command = DA.getInstance.getCommand(COMMAND_NAME);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        } else {
            DA.log.errorLog("Could not register command");
        }

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            this.sendHelp(sender);
        } else {
            switch (args[0].toLowerCase()) {
                case "info":
                    InfoCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                case "reload":
                    ReloadCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                case "list":
                    ListCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                case "give":
                    GiveCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                default:
                    this.sendHelp(sender);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage(DA.loader.languageReader.getComponent("Command_Assistance_Use"));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            ArrayList<String> list = new ArrayList<>();
            if (sender.hasPermission("drugsadder.cmd.info")) list.add("info");
            if (sender.hasPermission("drugsadder.cmd.reload")) list.add("reload");
            if (sender.hasPermission("drugsadder.cmd.list")) list.add("list");
            if (sender.hasPermission("drugsadder.cmd.give")) list.add("give");
            return list;
        } else {
            switch (args[0].toLowerCase()) {
                case "info":
                    if (sender.hasPermission("drugsadder.cmd.info")) {
                        return InfoCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                    }
                case "list":
                    if (sender.hasPermission("drugsadder.cmd.list")) {
                        return ListCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                    }
                case "give":
                    if (sender.hasPermission("drugsadder.cmd.give")) {
                        return GiveCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
                    }
            }
            return new ArrayList<>();
        }
    }
}
