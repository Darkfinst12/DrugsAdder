package de.darkfinst.drugsadder.commands;

import de.darkfinst.drugsadder.DA;
import lombok.Getter;
import net.kyori.adventure.text.Component;
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
            if (PossibleArgs.INFO.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.INFO.getPermission())) {
                InfoCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } else if (PossibleArgs.RELOAD.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.RELOAD.getPermission())) {
                ReloadCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } else if (PossibleArgs.LIST.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.LIST.getPermission())) {
                ListCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } else if (PossibleArgs.GIVE.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.GIVE.getPermission())) {
                GiveCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } else if (PossibleArgs.PLAYER.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.PLAYER.getPermission())) {
                PlayerCommand.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            } else {
                this.sendHelp(sender);
            }
        }
        return true;
    }

    private void sendHelp(CommandSender commandSender) {
        Component component = DA.loader.languageReader.getComponent("Command_Assistance_Use");
        if (component == null) {
            component = Component.text(DA.loader.languageReader.getString("Command_Assistance_Use"));
        }
        commandSender.sendMessage(component);
    }

    public static String buildCommand(String... args) {
        StringBuilder stringBuilder = new StringBuilder("/" + COMMAND_NAME);
        for (String arg : args) {
            stringBuilder.append(" ").append(arg);
        }
        return stringBuilder.toString();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            ArrayList<String> list = new ArrayList<>();
            if (sender.hasPermission(PossibleArgs.INFO.getPermission())) list.add(PossibleArgs.INFO.getArg());
            if (sender.hasPermission(PossibleArgs.RELOAD.getPermission())) list.add(PossibleArgs.RELOAD.getArg());
            if (sender.hasPermission(PossibleArgs.LIST.getPermission())) list.add(PossibleArgs.LIST.getArg());
            if (sender.hasPermission(PossibleArgs.GIVE.getPermission())) list.add(PossibleArgs.GIVE.getArg());
            if (sender.hasPermission(PossibleArgs.PLAYER.getPermission())) list.add(PossibleArgs.PLAYER.getArg());
            return list;
        } else {
            if (PossibleArgs.INFO.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.INFO.getPermission())) {
                return InfoCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            if (PossibleArgs.LIST.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.LIST.getPermission())) {
                return ListCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            if (PossibleArgs.GIVE.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.GIVE.getPermission())) {
                return GiveCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            if (PossibleArgs.PLAYER.getArg().equalsIgnoreCase(args[0].toLowerCase()) && sender.hasPermission(PossibleArgs.PLAYER.getPermission())) {
                return PlayerCommand.complete(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
            }
            return new ArrayList<>();
        }
    }

    @Getter
    public enum PossibleArgs {
        INFO("Command_Arg_Info", "drugsadder.cmd.info"),
        RELOAD("Command_Arg_Reload", "drugsadder.cmd.reload"),
        LIST("Command_Arg_List", "drugsadder.cmd.list"),
        GIVE("Command_Arg_Give", "drugsadder.cmd.give"),
        PLAYER("Command_Arg_Player", "drugsadder.cmd.player"),
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
